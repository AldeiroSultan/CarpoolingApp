package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.adapters.RideAdapter;
import com.carpoolingapp.models.Ride;
import com.carpoolingapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchRideActivity extends AppCompatActivity {

    private RecyclerView ridesRecyclerView;
    private View emptyState;
    private TextView emptyText;
    private MaterialButton demoButton, demoWalkthroughButton;
    private Spinner sortSpinner;
    private RideAdapter adapter;
    private List<Ride> rideList;
    private FirebaseHelper firebaseHelper;

    private String searchFrom, searchTo, searchDate;
    private String currentSortOption = "Cheapest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ride);

        searchFrom = getIntent().getStringExtra("from");
        searchTo = getIntent().getStringExtra("to");
        searchDate = getIntent().getStringExtra("date");

        initViews();
        setupToolbar();
        setupSortSpinner();
        setupRecyclerView();
        setupDemoButtons();
        searchRides();
    }

    private void initViews() {
        ridesRecyclerView = findViewById(R.id.ridesRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        emptyText = findViewById(R.id.emptyText);
        demoButton = findViewById(R.id.demoButton);
        demoWalkthroughButton = findViewById(R.id.demoWalkthroughButton);
        sortSpinner = findViewById(R.id.sortSpinner);
        firebaseHelper = FirebaseHelper.getInstance();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Results");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSortSpinner() {
        if (sortSpinner == null) return;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.sort_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = parent.getItemAtPosition(position).toString();
                sortRides();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        rideList = new ArrayList<>();
        adapter = new RideAdapter(this, rideList, new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride) {
                // Open ride details
                Intent intent = new Intent(SearchRideActivity.this, RideDetailActivity.class);
                intent.putExtra("rideId", ride.getRideId());
                intent.putExtra("driverId", ride.getDriverId());
                intent.putExtra("driverName", ride.getDriverName());
                intent.putExtra("from", ride.getFromLocation());
                intent.putExtra("to", ride.getToLocation());
                intent.putExtra("date", ride.getDate());
                intent.putExtra("time", ride.getTime());
                intent.putExtra("price", ride.getPricePerSeat());
                intent.putExtra("seats", ride.getAvailableSeats());
                startActivity(intent);
            }
        });
        ridesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ridesRecyclerView.setAdapter(adapter);
    }

    private void setupDemoButtons() {
        View.OnClickListener demoClickListener = v -> {
            Toast.makeText(SearchRideActivity.this, "Starting demo walkthrough...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SearchRideActivity.this, RideDetailActivity.class);
            intent.putExtra("isDemo", true);
            startActivity(intent);
        };

        if (demoButton != null) {
            demoButton.setOnClickListener(demoClickListener);
        }

        if (demoWalkthroughButton != null) {
            demoWalkthroughButton.setOnClickListener(demoClickListener);
        }
    }

    private void searchRides() {
        firebaseHelper.getRidesRef()
                .orderByChild("status")
                .equalTo("active")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        rideList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Ride ride = snapshot.getValue(Ride.class);
                            if (ride != null) {
                                ride.setRideId(snapshot.getKey());

                                if (!"hosting".equals(ride.getRideType())) {
                                    continue;
                                }

                                boolean matchesFrom = searchFrom == null ||
                                        ride.getFromLocation().toLowerCase().contains(searchFrom.toLowerCase());
                                boolean matchesTo = searchTo == null ||
                                        ride.getToLocation().toLowerCase().contains(searchTo.toLowerCase());
                                boolean matchesDate = searchDate == null ||
                                        ride.getDate().equals(searchDate);

                                if (matchesFrom && matchesTo && matchesDate) {
                                    rideList.add(ride);
                                }
                            }
                        }

                        sortRides();
                        adapter.notifyDataSetChanged();

                        if (rideList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            ridesRecyclerView.setVisibility(View.GONE);
                            if (emptyText != null) {
                                emptyText.setText("No rides found matching your search");
                            }
                        } else {
                            emptyState.setVisibility(View.GONE);
                            ridesRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SearchRideActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sortRides() {
        if ("Cheapest".equals(currentSortOption)) {
            Collections.sort(rideList, new Comparator<Ride>() {
                @Override
                public int compare(Ride r1, Ride r2) {
                    return Double.compare(r1.getPricePerSeat(), r2.getPricePerSeat());
                }
            });
        } else if ("Fastest".equals(currentSortOption)) {
            Collections.sort(rideList, new Comparator<Ride>() {
                @Override
                public int compare(Ride r1, Ride r2) {
                    return r1.getTime().compareTo(r2.getTime());
                }
            });
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}