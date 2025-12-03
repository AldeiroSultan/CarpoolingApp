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
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchRideActivity extends AppCompatActivity {

    private RecyclerView hostingRidesRecyclerView, requestRidesRecyclerView;
    private View emptyState;
    private TextView emptyText, hostingRidesHeader, requestRidesHeader;
    private Spinner sortSpinner, filterSpinner;
    private RideAdapter hostingAdapter, requestAdapter;
    private List<Ride> hostingRideList, requestRideList;
    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    private String searchFrom, searchTo, searchDate;
    private String currentSortOption = "Cheapest";
    private FilterType currentFilter = FilterType.ALL;

    private enum FilterType {
        ALL,
        HOSTING,
        REQUEST
    }

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
        setupFilterSpinner();
        setupRecyclerViews();
        searchRides();
    }

    private void initViews() {
        hostingRidesRecyclerView = findViewById(R.id.hostingRidesRecyclerView);
        requestRidesRecyclerView = findViewById(R.id.requestRidesRecyclerView);
        hostingRidesHeader = findViewById(R.id.hostingRidesHeader);
        requestRidesHeader = findViewById(R.id.requestRidesHeader);
        emptyState = findViewById(R.id.emptyState);
        emptyText = findViewById(R.id.emptyText);
        sortSpinner = findViewById(R.id.sortSpinner);
        filterSpinner = findViewById(R.id.filterSpinner);
        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
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

    private void setupFilterSpinner() {
        if (filterSpinner == null) return;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.ride_filter_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equalsIgnoreCase(getString(R.string.filter_option_available))) {
                    currentFilter = FilterType.HOSTING;
                } else if (selected.equalsIgnoreCase(getString(R.string.filter_option_requests))) {
                    currentFilter = FilterType.REQUEST;
                } else {
                    currentFilter = FilterType.ALL;
                }
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerViews() {
        hostingRideList = new ArrayList<>();
        requestRideList = new ArrayList<>();

        RideAdapter.OnRideClickListener onRideClickListener = ride -> {
            Intent intent = new Intent(SearchRideActivity.this, RideDetailActivity.class);
            intent.putExtra("rideId", ride.getRideId());
            startActivity(intent);
        };

        hostingAdapter = new RideAdapter(this, hostingRideList, onRideClickListener);
        hostingRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        hostingRidesRecyclerView.setAdapter(hostingAdapter);
        hostingRidesRecyclerView.setNestedScrollingEnabled(false);

        requestAdapter = new RideAdapter(this, requestRideList, onRideClickListener);
        requestRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestRidesRecyclerView.setAdapter(requestAdapter);
        requestRidesRecyclerView.setNestedScrollingEnabled(false);
    }


    private void searchRides() {
        firebaseHelper.getRidesRef()
                .orderByChild("status")
                .equalTo("active")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hostingRideList.clear();
                        requestRideList.clear();

                        String currentUserId = prefsHelper.getUserId();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Ride ride = snapshot.getValue(Ride.class);
                            if (ride == null) continue;

                            ride.setRideId(snapshot.getKey());

                            // Exclude listings created by the current user (both hosting and requests)
                            if (currentUserId != null &&
                                    ride.getDriverId() != null &&
                                    currentUserId.equals(ride.getDriverId())) {
                                continue;
                            }

                            // Exclude hosting rides with no remaining seats
                            if ("hosting".equals(ride.getRideType()) &&
                                    ride.getAvailableSeats() <= 0) {
                                continue;
                            }

                            boolean matchesFrom = searchFrom == null ||
                                    ride.getFromLocation().toLowerCase().contains(searchFrom.toLowerCase());
                            boolean matchesTo = searchTo == null ||
                                    ride.getToLocation().toLowerCase().contains(searchTo.toLowerCase());
                            boolean matchesDate = searchDate == null || searchDate.isEmpty() ||
                                    ride.getDate().equals(searchDate);

                            if (matchesFrom && matchesTo && matchesDate) {
                                if ("hosting".equals(ride.getRideType())) {
                                    hostingRideList.add(ride);
                                } else if ("request".equals(ride.getRideType())) {
                                    requestRideList.add(ride);
                                }
                            }
                        }

                        sortRides();
                        updateUI();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SearchRideActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        hostingAdapter.notifyDataSetChanged();
        requestAdapter.notifyDataSetChanged();

        boolean hasHosting = !hostingRideList.isEmpty();
        boolean hasRequests = !requestRideList.isEmpty();

        boolean showHosting = currentFilter == FilterType.ALL || currentFilter == FilterType.HOSTING;
        boolean showRequests = currentFilter == FilterType.ALL || currentFilter == FilterType.REQUEST;

        boolean hostingVisible = showHosting && hasHosting;
        boolean requestVisible = showRequests && hasRequests;

        hostingRidesRecyclerView.setVisibility(hostingVisible ? View.VISIBLE : View.GONE);
        hostingRidesHeader.setVisibility(hostingVisible ? View.VISIBLE : View.GONE);
        requestRidesRecyclerView.setVisibility(requestVisible ? View.VISIBLE : View.GONE);
        requestRidesHeader.setVisibility(requestVisible ? View.VISIBLE : View.GONE);

        boolean hasVisibleResults = hostingVisible || requestVisible;

        if (!hasVisibleResults) {
            emptyState.setVisibility(View.VISIBLE);
            emptyText.setText(getEmptyMessageForFilter());
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    private String getEmptyMessageForFilter() {
        switch (currentFilter) {
            case HOSTING:
                return getString(R.string.empty_available_rides);
            case REQUEST:
                return getString(R.string.empty_request_rides);
            default:
                return getString(R.string.empty_all_rides);
        }
    }

    private void sortRides() {
        Comparator<Ride> comparator = (r1, r2) -> {
            if ("Cheapest".equals(currentSortOption)) {
                return Double.compare(r1.getPricePerSeat(), r2.getPricePerSeat());
            } else if ("Upcoming".equals(currentSortOption)) {
                return r1.getTime().compareTo(r2.getTime());
            }
            return 0;
        };

        Collections.sort(hostingRideList, comparator);
        Collections.sort(requestRideList, comparator);

        hostingAdapter.notifyDataSetChanged();
        requestAdapter.notifyDataSetChanged();
    }
}
