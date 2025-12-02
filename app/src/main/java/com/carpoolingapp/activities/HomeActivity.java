package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;
import com.carpoolingapp.adapters.RideAdapter;
import com.carpoolingapp.models.Ride;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameText;
    private MaterialButton myBookingsButton, myListingsButton;
    private RecyclerView recyclerView;
    private View emptyState, searchCard;
    private BottomNavigationView bottomNav;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;
    private RideAdapter adapter;
    private List<Ride> rideList;

    private boolean isBookingsMode = true; // true = My Bookings, false = My Listings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initFirebase();
        setupListeners();
        setupBottomNav();
        loadUserData();
        setupRecyclerView();
        loadRides();
    }

    private void initViews() {
        userNameText = findViewById(R.id.userNameText);
        TextView userNameText2 = findViewById(R.id.helloText);
        myBookingsButton = findViewById(R.id.myBookingsButton);
        myListingsButton = findViewById(R.id.myListingsButton);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        searchCard = findViewById(R.id.searchView);

        // emptyState might not exist in layout, check before using
        emptyState = findViewById(R.id.emptyState);
    }

    private void initFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
    }

    private void setupListeners() {
        if (myBookingsButton != null) {
            myBookingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToBookingsMode();
                }
            });
        }

        if (myListingsButton != null) {
            myListingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchToListingsMode();
                }
            });
        }

        if (searchCard != null) {
            searchCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open search form activity
                    Intent intent = new Intent(HomeActivity.this, SearchFormActivity.class);
                    startActivity(intent);
                }
            });
        }

        View profileImage = findViewById(R.id.profileImage);
        if (profileImage != null) {
            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                }
            });
        }
    }

    private void setupBottomNav() {
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_create) {
                try {
                    Intent intent = new Intent(HomeActivity.this, CreateRideActivity.class);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Error opening Create Ride", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return false;
                }
            } else if (itemId == R.id.nav_messages) {
                try {
                    Intent intent = new Intent(HomeActivity.this, MessagesActivity.class);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Error opening Messages", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return false;
                }
            } else if (itemId == R.id.nav_profile) {
                try {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, "Error opening Profile", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        });
    }

    private void loadUserData() {
        String userName = prefsHelper.getUserName();
        userNameText.setText(userName.split(" ")[0]); // only show first name
    }

    private void setupRecyclerView() {
        if (recyclerView == null) return;

        rideList = new ArrayList<>();
        adapter = new RideAdapter(this, rideList, new RideAdapter.OnRideClickListener() {
            @Override
            public void onRideClick(Ride ride) {
                Toast.makeText(HomeActivity.this, "Ride: " + ride.getFromLocation() + " to " + ride.getToLocation(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void switchToBookingsMode() {
        isBookingsMode = true;
        updateModeUI();
        loadRides();
    }

    private void switchToListingsMode() {
        isBookingsMode = false;
        updateModeUI();
        loadRides();
    }

    private void updateModeUI() {
        if (isBookingsMode) {
            myBookingsButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            myBookingsButton.setTextColor(getColor(R.color.white));
            myListingsButton.setBackgroundTintList(getColorStateList(R.color.status_inactive));
            myListingsButton.setTextColor(getColor(R.color.primary_blue));
        } else {
            myListingsButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            myListingsButton.setTextColor(getColor(R.color.white));
            myBookingsButton.setBackgroundTintList(getColorStateList(R.color.status_inactive));
            myBookingsButton.setTextColor(getColor(R.color.primary_blue));
        }
    }

    private void loadRides() {
        String userId = prefsHelper.getUserId();
        if (userId == null || recyclerView == null || adapter == null) return;

        if (isBookingsMode) {
            // My Bookings: Show rides where I'm looking for a ride (rideType = "looking")
            firebaseHelper.getRidesRef()
                    .orderByChild("driverId")
                    .equalTo(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            rideList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Ride ride = snapshot.getValue(Ride.class);
                                if (ride != null && "looking".equals(ride.getRideType())) {
                                    ride.setRideId(snapshot.getKey());
                                    rideList.add(ride);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (emptyState != null && recyclerView != null) {
                                if (rideList.isEmpty()) {
                                    emptyState.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    emptyState.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(HomeActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // My Listings: Show rides where I'm hosting (rideType = "hosting")
            firebaseHelper.getRidesRef()
                    .orderByChild("driverId")
                    .equalTo(userId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            rideList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Ride ride = snapshot.getValue(Ride.class);
                                if (ride != null && "hosting".equals(ride.getRideType())) {
                                    ride.setRideId(snapshot.getKey());
                                    rideList.add(ride);
                                }
                            }

                            adapter.notifyDataSetChanged();

                            if (emptyState != null && recyclerView != null) {
                                if (rideList.isEmpty()) {
                                    emptyState.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    emptyState.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(HomeActivity.this, "Failed to load listings", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        loadRides();
    }
}