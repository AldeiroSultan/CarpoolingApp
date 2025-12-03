package com.carpoolingapp.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.carpoolingapp.R;
import com.carpoolingapp.models.Booking;
import com.carpoolingapp.models.Ride;
import com.carpoolingapp.models.User;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RideDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView fromText, toText, dateText, timeText, priceText, seatsText, driverNameText, ratingText, numRidesText;
    private MaterialButton bookNowButton;
    private MaterialButton cancelRideButton;
    private MapView mapView;
    private GoogleMap googleMap;

    private SharedPrefsHelper prefsHelper;
    private FirebaseHelper firebaseHelper;
    private String rideId;
    private String driverId;
    private String driverName;
    private Ride currentRide;
    private boolean isMapReady = false;
    private boolean isManageMode = false;
    private String mode; // "driver_manage", "rider_manage", or null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        prefsHelper = new SharedPrefsHelper(this);
        firebaseHelper = FirebaseHelper.getInstance();

        // Determine if this screen is opened in "manage" mode (from Home listings)
        isManageMode = getIntent().getBooleanExtra("isManageMode", false);
        mode = getIntent().getStringExtra("mode");
        if ("driver_manage".equals(mode)) {
            isManageMode = true;
        }

        initViews();
        setupToolbar();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        loadRideData();
        setupListeners();
    }

    private void initViews() {
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        priceText = findViewById(R.id.priceText);
        seatsText = findViewById(R.id.seatsText);
        driverNameText = findViewById(R.id.driverNameText);
        ratingText = findViewById(R.id.ratingText);
        numRidesText = findViewById(R.id.numRidesText);
        bookNowButton = findViewById(R.id.bookNowButton);
        cancelRideButton = findViewById(R.id.cancelRideButton);
        mapView = findViewById(R.id.mapView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isManageMode ? "Manage Ride" : "Ride Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadRideData() {
        boolean isDemo = getIntent().getBooleanExtra("isDemo", false);

        if (isDemo) {
            loadDemoData();
            // In demo mode we keep the original booking behavior
            return;
        }

        // Load actual ride data from Firebase using rideId
        rideId = getIntent().getStringExtra("rideId");
        if (rideId == null || rideId.isEmpty()) {
            Toast.makeText(this, "Ride not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper.getRideRef(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentRide = dataSnapshot.getValue(Ride.class);
                if (currentRide == null) {
                    Toast.makeText(RideDetailActivity.this, "Ride details unavailable", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                driverId = currentRide.getDriverId();
                driverName = currentRide.getDriverName();

                fromText.setText(currentRide.getFromLocation());
                toText.setText(currentRide.getToLocation());
                dateText.setText(currentRide.getDate());
                timeText.setText(currentRide.getTime());
                priceText.setText("$" + String.format(Locale.US, "%.2f", currentRide.getPricePerSeat()));
                seatsText.setText(currentRide.getAvailableSeats() + " seats available");
                driverNameText.setText(driverName != null ? driverName : "Driver");

                loadDriverStats();

                // Configure actions depending on whether this is a management screen
                configureActionsForMode();

                // Once data is loaded, refresh map if it's already ready
                if (googleMap != null && isMapReady) {
                    updateMapForCurrentRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RideDetailActivity.this, "Failed to load ride", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadDriverStats() {
        if (driverId == null || driverId.isEmpty()) {
            return;
        }

        firebaseHelper.getUserRef(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null) {
                    return;
                }

                double rating = user.getRating();
                int totalRides = user.getTotalRides();

                if (ratingText != null) {
                    if (rating > 0) {
                        String ratingStr = String.format(Locale.US, "%.1f", rating);
                        ratingText.setText(ratingStr);
                    } else {
                        ratingText.setText("0.0");
                    }
                }

                if (numRidesText != null) {
                    if (totalRides > 0) {
                        numRidesText.setText(totalRides + " rides");
                    } else {
                        numRidesText.setText("0 rides");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Ignore rating errors for now
            }
        });
    }

    private void loadDemoData() {
        fromText.setText("Downtown Vancouver");
        toText.setText("Surrey Central");
        dateText.setText("Dec 15, 2024");
        timeText.setText("10:00 AM");
        priceText.setText("$15.00");
        seatsText.setText("3 seats available");
        driverNameText.setText("Demo Driver");
        driverName = "Demo Driver";
        // Demo rides are always viewed, not managed
        configureActionsForMode();
    }

    private void setupListeners() {
        bookNowButton.setOnClickListener(v -> {
            if (isManageMode) {
                openEditRideScreen();
            } else {
                bookRide();
            }
        });

        if (cancelRideButton != null) {
            cancelRideButton.setOnClickListener(v -> {
                if ("rider_manage".equals(mode)) {
                    confirmAndCancelBooking();
                } else {
                    confirmAndCancelRide();
                }
            });
        }
    }

    /**
     * Show or hide management actions (Edit / Cancel) depending on the current user and ride type.
     */
    private void configureActionsForMode() {
        if (bookNowButton == null) return;

        // Rider-manage mode (manage a booking)
        if ("rider_manage".equals(mode)) {
            bookNowButton.setVisibility(View.GONE);
            if (cancelRideButton != null) {
                cancelRideButton.setText("Cancel Booking");
                cancelRideButton.setVisibility(View.VISIBLE);
            }
            int seats = getIntent().getIntExtra("seatsBooked", 1);
            seatsText.setText(seats + " booked");
            return;
        }

        if (!isManageMode) {
            // Normal booking mode
            bookNowButton.setVisibility(View.VISIBLE);
            bookNowButton.setText("Book Now");
            if (cancelRideButton != null) {
                cancelRideButton.setVisibility(View.GONE);
            }
            return;
        }

        // Manage mode – only the ride owner can edit/cancel
        String currentUserId = prefsHelper.getUserId();
        boolean isOwner = currentRide != null
                && currentUserId != null
                && currentUserId.equals(currentRide.getDriverId());

        if (isOwner) {
            bookNowButton.setText("Edit Ride");
            if (cancelRideButton != null) {
                cancelRideButton.setVisibility(View.VISIBLE);
            }
        } else {
            // Not owner – fallback to read‑only booking behavior
            isManageMode = false;
            bookNowButton.setText("Book Now");
            if (cancelRideButton != null) {
                cancelRideButton.setVisibility(View.GONE);
            }
        }
    }

    private void openEditRideScreen() {
        if (currentRide == null || rideId == null) {
            Toast.makeText(this, "Ride details unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RideDetailActivity.this, CreateRideActivity.class);
        intent.putExtra("rideId", rideId);
        intent.putExtra("isEditMode", true);
        startActivity(intent);
    }

    private void confirmAndCancelRide() {
        if (currentRide == null || rideId == null) {
            Toast.makeText(this, "Ride details unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cancel ride")
                .setMessage("Are you sure you want to cancel this ride? It will no longer appear in search results.")
                .setPositiveButton("Yes, cancel", (dialog, which) -> cancelRide())
                .setNegativeButton("No", null)
                .show();
    }

    private void confirmAndCancelBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> cancelBooking())
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelRide() {
        currentRide.setStatus("cancelled");
        currentRide.setUpdatedAt(System.currentTimeMillis());

        firebaseHelper.getRideRef(rideId)
                .setValue(currentRide)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RideDetailActivity.this, "Ride cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RideDetailActivity.this, "Failed to cancel ride", Toast.LENGTH_SHORT).show());
    }

    private void cancelBooking() {
        String currentUserId = prefsHelper.getUserId();
        if (currentUserId == null || rideId == null) {
            Toast.makeText(this, "Booking details unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        int seatsToRestore = getIntent().getIntExtra("seatsBooked", 1);

        firebaseHelper.getBookingsRef()
                .orderByChild("rideId").equalTo(rideId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean deletedAny = false;
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Booking booking = snap.getValue(Booking.class);
                            if (booking != null && currentUserId.equals(booking.getRiderId())) {
                                snap.getRef().removeValue();
                                deletedAny = true;
                            }
                        }

                        if (deletedAny) {
                            restoreRideSeats(seatsToRestore);
                            Toast.makeText(RideDetailActivity.this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RideDetailActivity.this, "No booking found to cancel", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RideDetailActivity.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void restoreRideSeats(int seatsToRestore) {
        if (rideId == null || seatsToRestore <= 0) return;

        firebaseHelper.getRideRef(rideId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                if (ride == null) return;

                int available = ride.getAvailableSeats();
                available = Math.max(0, available + seatsToRestore);
                ride.setAvailableSeats(available);

                firebaseHelper.getRideRef(rideId).setValue(ride);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void bookRide() {
        boolean isDemo = getIntent().getBooleanExtra("isDemo", false);

        // Check if user is trying to book their own ride
        String currentUserId = prefsHelper.getUserId();

        if (currentUserId != null && currentUserId.equals(driverId)) {
            Toast.makeText(this, "You cannot book your own ride!", Toast.LENGTH_LONG).show();
            return;
        }

        // Demo rides: always book 1 seat with provided price
        if (isDemo || currentRide == null) {
            proceedToPayment(1, isDemo);
            return;
        }

        int availableSeats = currentRide.getAvailableSeats();
        if (availableSeats <= 1) {
            proceedToPayment(1, false);
        } else {
            showSeatSelectionDialog(availableSeats);
        }
    }

    private void showSeatSelectionDialog(int maxSeats) {
        if (maxSeats <= 0) {
            Toast.makeText(this, "No seats available for booking", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = new String[maxSeats];
        for (int i = 0; i < maxSeats; i++) {
            int seats = i + 1;
            options[i] = seats + (seats == 1 ? " seat" : " seats");
        }

        final int[] selectedIndex = {0};

        new AlertDialog.Builder(this)
                .setTitle("Select seats to book")
                .setSingleChoiceItems(options, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    int seatsToBook = selectedIndex[0] + 1;
                    proceedToPayment(seatsToBook, false);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void proceedToPayment(int seatsToBook, boolean isDemo) {
        if (seatsToBook <= 0) seatsToBook = 1;

        // Use the ride's actual price per seat where possible
        double pricePerSeat = currentRide != null
                ? currentRide.getPricePerSeat()
                : getIntent().getDoubleExtra("price", 15.00);
        double totalPrice = pricePerSeat * seatsToBook;

        // Go to payment
        Intent intent = new Intent(RideDetailActivity.this, PaymentActivity.class);
        intent.putExtra("totalPrice", totalPrice);
        intent.putExtra("seatsBooked", seatsToBook);
        intent.putExtra("isDemo", isDemo);
        intent.putExtra("rideId", rideId);
        intent.putExtra("driverId", driverId);
        intent.putExtra("driverName", driverName);
        intent.putExtra("from", fromText.getText().toString());
        intent.putExtra("to", toText.getText().toString());
        intent.putExtra("date", dateText.getText().toString());
        intent.putExtra("time", timeText.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        isMapReady = true;
        updateMapForCurrentRide();
    }

    private void updateMapForCurrentRide() {
        if (!isMapReady || currentRide == null) {
            return;
        }

        String fromLocationName = fromText.getText().toString();
        String toLocationName = toText.getText().toString();

        // Prefer ride's stored coordinates when available
        LatLng fromLatLng = null;
        LatLng toLatLng = null;

        if (currentRide != null) {
            if (currentRide.getFromLat() != 0 && currentRide.getFromLng() != 0) {
                fromLatLng = new LatLng(currentRide.getFromLat(), currentRide.getFromLng());
            }
            if (currentRide.getToLat() != 0 && currentRide.getToLng() != 0) {
                toLatLng = new LatLng(currentRide.getToLat(), currentRide.getToLng());
            }
        }

        // If any coordinate is missing, fall back to Geocoder for that point
        if ((fromLatLng == null || toLatLng == null) && (!fromLocationName.isEmpty() || !toLocationName.isEmpty())) {
            LatLng finalFromLatLng = fromLatLng;
            LatLng finalToLatLng = toLatLng;

            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                    LatLng geocodedFrom = finalFromLatLng;
                    LatLng geocodedTo = finalToLatLng;

                    if (geocodedFrom == null && !fromLocationName.isEmpty()) {
                        List<Address> fromAddresses = geocoder.getFromLocationName(fromLocationName, 1);
                        if (fromAddresses != null && !fromAddresses.isEmpty()) {
                            Address fromAddress = fromAddresses.get(0);
                            geocodedFrom = new LatLng(fromAddress.getLatitude(), fromAddress.getLongitude());
                        }
                    }

                    if (geocodedTo == null && !toLocationName.isEmpty()) {
                        List<Address> toAddresses = geocoder.getFromLocationName(toLocationName, 1);
                        if (toAddresses != null && !toAddresses.isEmpty()) {
                            Address toAddress = toAddresses.get(0);
                            geocodedTo = new LatLng(toAddress.getLatitude(), toAddress.getLongitude());
                        }
                    }

                    LatLng finalGeocodedFrom = geocodedFrom;
                    LatLng finalGeocodedTo = geocodedTo;

                    runOnUiThread(() -> updateMapMarkers(finalGeocodedFrom, finalGeocodedTo, fromLocationName, toLocationName));
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(RideDetailActivity.this, "Map service not available.", Toast.LENGTH_SHORT).show());
                }
            }).start();
        } else {
            updateMapMarkers(fromLatLng, toLatLng, fromLocationName, toLocationName);
        }
    }

    private void updateMapMarkers(LatLng fromLatLng, LatLng toLatLng, String fromLocationName, String toLocationName) {
        if (googleMap == null) return;

        googleMap.clear();

        boolean hasAnyMarker = false;

        if (fromLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(fromLatLng)
                    .title("From: " + fromLocationName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            hasAnyMarker = true;
        }

        if (toLatLng != null) {
            googleMap.addMarker(new MarkerOptions()
                    .position(toLatLng)
                    .title("To: " + toLocationName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            hasAnyMarker = true;
        }

        if (fromLatLng != null && toLatLng != null) {
            googleMap.addPolyline(new PolylineOptions()
                    .add(fromLatLng, toLatLng)
                    .width(5)
                    .color(getResources().getColor(R.color.primary_blue)));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(fromLatLng);
            builder.include(toLatLng);
            LatLngBounds bounds = builder.build();
            int padding = 150; // offset from edges of the map in pixels
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } else if (fromLatLng != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fromLatLng, 12f));
        } else if (toLatLng != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toLatLng, 12f));
        }

        if (!hasAnyMarker) {
            Toast.makeText(this, "Could not find locations on the map.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}