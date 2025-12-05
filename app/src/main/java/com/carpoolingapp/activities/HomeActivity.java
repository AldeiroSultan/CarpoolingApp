package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carpoolingapp.R;
import com.carpoolingapp.adapters.RideRequestAdapter;
import com.carpoolingapp.adapters.UnifiedAdapter;
import com.carpoolingapp.models.Booking;
import com.carpoolingapp.models.Ride;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameText;
    private MaterialButton myActiveListingsButton, myRideRequestsButton;
    private RecyclerView recyclerView;
    private View emptyState, searchCard;
    private BottomNavigationView bottomNav;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    // Unified list (Rides + Bookings)
    private List<Object> unifiedList = new ArrayList<>();
    private UnifiedAdapter unifiedAdapter;

    // Ride requests mode list
    private List<Ride> rideRequestsList = new ArrayList<>();
    private RideRequestAdapter rideRequestAdapter;
    private int pendingLoads = 0;


    private boolean isListingsMode = true; // true = Active Listings, false = Ride Requests

    // Formats for parsing timestamps
    private static final List<String> DATE_PATTERNS = Arrays.asList("MMM dd, yyyy", "yyyy-MM-dd", "dd/MM/yyyy");
    private static final List<String> TIME_PATTERNS = Arrays.asList("hh:mm a", "HH:mm", "HH:mm:ss");

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
        updateModeUI();
    }

    private void initViews() {
        userNameText = findViewById(R.id.userNameText);
        myActiveListingsButton = findViewById(R.id.myActiveListingsButton);
        myRideRequestsButton = findViewById(R.id.myRideRequestsButton);
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
        myActiveListingsButton.setOnClickListener(v -> {
            isListingsMode = true;
            updateModeUI();
            loadData();
        });

        myRideRequestsButton.setOnClickListener(v -> {
            isListingsMode = false;
            updateModeUI();
            loadData();
        });

        searchCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SearchFormActivity.class));
        });

        findViewById(R.id.profileImage).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }

    private void setupRecyclerView() {
        unifiedAdapter = new UnifiedAdapter(unifiedList, this);
        rideRequestAdapter = new RideRequestAdapter(this, rideRequestsList, ride -> {
            Intent intent = new Intent(this, RideDetailActivity.class);
            intent.putExtra("rideId", ride.getRideId());
            intent.putExtra("mode", "driver_manage");
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;

            try {
                if (id == R.id.nav_create) {
                    startActivity(new Intent(HomeActivity.this, CreateRideActivity.class));
                } else if (id == R.id.nav_messages) {
                    startActivity(new Intent(HomeActivity.this, MessagesActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                }
                finish();
                return true;

            } catch (Exception e) {
                Toast.makeText(HomeActivity.this, "Error opening screen", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void loadUserData() {
        userNameText.setText(prefsHelper.getUserName().split(" ")[0]);
    }

    private void updateModeUI() {
        if (isListingsMode) {
            myActiveListingsButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            myActiveListingsButton.setTextColor(getColor(R.color.white));

            myRideRequestsButton.setBackgroundTintList(getColorStateList(R.color.status_inactive));
            myRideRequestsButton.setTextColor(getColor(R.color.primary_blue));
        } else {
            myRideRequestsButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            myRideRequestsButton.setTextColor(getColor(R.color.white));

            myActiveListingsButton.setBackgroundTintList(getColorStateList(R.color.status_inactive));
            myActiveListingsButton.setTextColor(getColor(R.color.primary_blue));
        }
    }

    private void loadData() {
        String userId = prefsHelper.getUserId();
        if (userId == null) return;

        if (isListingsMode) {
            loadUnifiedListings(userId);
        } else {
            loadMyRideRequests(userId);
        }
    }

    private void loadUnifiedListings(String userId) {

        unifiedList.clear();
        recyclerView.setAdapter(unifiedAdapter);

        // Load hosted rides
        firebaseHelper.getRidesRef()
                .orderByChild("driverId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {

                        for (DataSnapshot snap : ds.getChildren()) {
                            Ride ride = snap.getValue(Ride.class);
                            if (ride != null && "hosting".equals(ride.getRideType()) && "active".equalsIgnoreCase(ride.getStatus())) {
                                ride.setRideId(snap.getKey());
                                unifiedList.add(ride);
                            }
                        }
                        unifiedAdapter.notifyDataSetChanged();
                        updateEmptyState(unifiedList.isEmpty());
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Load bookings (user as rider)
        firebaseHelper.getBookingsRef()
                .orderByChild("riderId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {

                        for (DataSnapshot snap : ds.getChildren()) {
                            Booking booking = snap.getValue(Booking.class);
                            if (booking != null) {
                                // Skip cancelled bookings so cancelled rides don't appear in the list
                                String status = booking.getStatus();
                                if ("cancelled".equalsIgnoreCase(status)) {
                                    continue;
                                }
                                booking.setBookingId(snap.getKey());
                                unifiedList.add(booking);
                            }
                        }

                        sortUnifiedList();
                        unifiedAdapter.notifyDataSetChanged();
                        updateEmptyState(unifiedList.isEmpty());
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }



    private void sortUnifiedList() {
        Collections.sort(unifiedList, (o1, o2) -> {
            long t1 = getTimestamp(o1);
            long t2 = getTimestamp(o2);
            return Long.compare(t1, t2);
        });
    }

    private long getTimestamp(Object obj) {
        if (obj instanceof Ride) {
            Ride r = (Ride) obj;
            return parseScheduledTimestamp(r.getDate(), r.getTime());
        }
        if (obj instanceof Booking) {
            Booking b = (Booking) obj;
            return parseScheduledTimestamp(b.getDate(), b.getTime());
        }
        return Long.MIN_VALUE;
    }

    private long parseScheduledTimestamp(String dateValue, String timeValue) {
        if (dateValue == null || dateValue.trim().isEmpty()) return Long.MIN_VALUE;

        for (String datePattern : DATE_PATTERNS) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.getDefault());
            try {
                Date parsedDate = dateFormat.parse(dateValue);
                if (parsedDate == null) continue;

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(parsedDate);

                if (!applyTime(calendar, timeValue)) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                }

                return calendar.getTimeInMillis();

            } catch (ParseException ignored) {}
        }
        return Long.MIN_VALUE;
    }

    private boolean applyTime(Calendar calendar, String timeValue) {
        if (timeValue == null || timeValue.trim().isEmpty()) return false;

        for (String pattern : TIME_PATTERNS) {
            SimpleDateFormat fmt = new SimpleDateFormat(pattern, Locale.getDefault());
            try {
                Date parsed = fmt.parse(timeValue);
                if (parsed == null) continue;

                Calendar c = Calendar.getInstance();
                c.setTime(parsed);

                calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                return true;

            } catch (ParseException ignored) {}
        }
        return false;
    }

    private void loadMyRideRequests(String userId) {
        recyclerView.setAdapter(rideRequestAdapter);

        firebaseHelper.getRidesRef()
                .orderByChild("driverId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {
                        rideRequestsList.clear();

                        for (DataSnapshot snap : ds.getChildren()) {
                            Ride ride = snap.getValue(Ride.class);
                            if (ride != null && "request".equals(ride.getRideType()) && "active".equalsIgnoreCase(ride.getStatus())) {
                                ride.setRideId(snap.getKey());
                                rideRequestsList.add(ride);
                            }
                        }

                        rideRequestAdapter.notifyDataSetChanged();
                        updateEmptyState(rideRequestsList.isEmpty());
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
        // Reload data when returning from other screens (e.g., after creating a ride)
        loadData();
    }
}
