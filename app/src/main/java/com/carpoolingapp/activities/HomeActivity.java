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
import com.carpoolingapp.adapters.BookingAdapter;
import com.carpoolingapp.adapters.RideAdapter;
import com.carpoolingapp.models.Booking;
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
    private RideAdapter rideAdapter;
    private BookingAdapter bookingAdapter;
    private List<Ride> rideList;
    private List<Booking> bookingList;

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
        loadData();
    }

    private void initViews() {
        userNameText = findViewById(R.id.userNameText);
        myBookingsButton = findViewById(R.id.myBookingsButton);
        myListingsButton = findViewById(R.id.myListingsButton);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        searchCard = findViewById(R.id.searchView);
        emptyState = findViewById(R.id.emptyState);
    }

    private void initFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
    }

    private void setupListeners() {
        if (myBookingsButton != null) {
            myBookingsButton.setOnClickListener(v -> switchToBookingsMode());
        }

        if (myListingsButton != null) {
            myListingsButton.setOnClickListener(v -> switchToListingsMode());
        }

        if (searchCard != null) {
            searchCard.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SearchFormActivity.class);
                startActivity(intent);
            });
        }

        View profileImage = findViewById(R.id.profileImage);
        if (profileImage != null) {
            profileImage.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
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
                startActivity(new Intent(HomeActivity.this, CreateRideActivity.class));
                return true;
            } else if (itemId == R.id.nav_messages) {
                startActivity(new Intent(HomeActivity.this, MessagesActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        if (userNameText != null) {
            String userName = prefsHelper.getUserName();
            userNameText.setText(userName);
        }
    }

    private void setupRecyclerView() {
        if (recyclerView == null) return;

        // Setup for both adapters
        rideList = new ArrayList<>();
        bookingList = new ArrayList<>();

        // Ride adapter for "My Listings"
        rideAdapter = new RideAdapter(this, rideList, ride ->
                Toast.makeText(HomeActivity.this,
                        "Ride: " + ride.getFromLocation() + " to " + ride.getToLocation(),
                        Toast.LENGTH_SHORT).show());

        // Booking adapter for "My Bookings"
        bookingAdapter = new BookingAdapter(this, bookingList, booking ->
                Toast.makeText(HomeActivity.this,
                        "Booking: " + booking.getFromLocation() + " to " + booking.getToLocation(),
                        Toast.LENGTH_SHORT).show());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void switchToBookingsMode() {
        isBookingsMode = true;
        updateModeUI();
        loadData();
    }

    private void switchToListingsMode() {
        isBookingsMode = false;
        updateModeUI();
        loadData();
    }

    private void updateModeUI() {
        if (myBookingsButton != null && myListingsButton != null) {
            if (isBookingsMode) {
                myBookingsButton.setBackgroundTintList(getColorStateList(R.color.status_active));
                myBookingsButton.setTextColor(getColor(R.color.white));
                myListingsButton.setBackgroundTintList(null);
                myListingsButton.setTextColor(getColor(R.color.primary_blue));
            } else {
                myListingsButton.setBackgroundTintList(getColorStateList(R.color.status_active));
                myListingsButton.setTextColor(getColor(R.color.white));
                myBookingsButton.setBackgroundTintList(null);
                myBookingsButton.setTextColor(getColor(R.color.primary_blue));
            }
        }
    }

    private void loadData() {
        String userId = prefsHelper.getUserId();
        if (userId == null) return;

        if (isBookingsMode) {
            loadMyBookings(userId);
        } else {
            loadMyListings(userId);
        }
    }

    private void loadMyBookings(String userId) {
        // Switch to booking adapter
        if (recyclerView != null) {
            recyclerView.setAdapter(bookingAdapter);
        }

        // Load bookings where current user is the rider
        firebaseHelper.getBookingsRef()
                .orderByChild("riderId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bookingList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Booking booking = snapshot.getValue(Booking.class);
                            if (booking != null) {
                                booking.setBookingId(snapshot.getKey());
                                bookingList.add(booking);
                            }
                        }

                        bookingAdapter.notifyDataSetChanged();
                        updateEmptyState(bookingList.isEmpty());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(HomeActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyListings(String userId) {
        // Switch to ride adapter
        if (recyclerView != null) {
            recyclerView.setAdapter(rideAdapter);
        }

        // Load rides where current user is hosting (rideType = "hosting")
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

                        rideAdapter.notifyDataSetChanged();
                        updateEmptyState(rideList.isEmpty());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(HomeActivity.this, "Failed to load listings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyState != null && recyclerView != null) {
            if (isEmpty) {
                emptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        loadData();
    }
}