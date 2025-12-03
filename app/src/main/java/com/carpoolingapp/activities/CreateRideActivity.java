package com.carpoolingapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.carpoolingapp.models.Ride;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateRideActivity extends AppCompatActivity {

    private EditText fromEditText, toEditText, seatsEditText, priceEditText;
    private TextView dateText, timeText;
    private MaterialButton createRideButton;
    private MaterialButton lookingForRideButton, hostingRideButton;
    private View dateLayout, timeLayout;
    private BottomNavigationView bottomNav;

    // NEW: Amenity CheckBoxes
    private CheckBox luggageCheckBox, petsCheckBox, bikesCheckBox, snowboardsCheckBox;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    private String selectedDate = "";
    private String selectedTime = "";
    private boolean isHostingRide = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_ride);

        initViews();
        initFirebase();
        setupToolbar();
        setupListeners();
        setupBottomNav();
        updateToggleUI();
    }

    private void initViews() {
        fromEditText = findViewById(R.id.fromEditText);
        toEditText = findViewById(R.id.toEditText);
        seatsEditText = findViewById(R.id.seatsEditText);
        priceEditText = findViewById(R.id.priceEditText);
        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        createRideButton = findViewById(R.id.createRideButton);
        dateLayout = findViewById(R.id.dateLayout);
        timeLayout = findViewById(R.id.timeLayout);
        bottomNav = findViewById(R.id.bottomNav);
        lookingForRideButton = findViewById(R.id.lookingForRideButton);
        hostingRideButton = findViewById(R.id.hostingRideButton);

        // NEW: Initialize amenity checkboxes
        luggageCheckBox = findViewById(R.id.luggageCheckBox);
        petsCheckBox = findViewById(R.id.petsCheckBox);
        bikesCheckBox = findViewById(R.id.bikesCheckBox);
        snowboardsCheckBox = findViewById(R.id.snowboardsCheckBox);
    }

    private void initFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Plan your ride");
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupListeners() {
        lookingForRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHostingRide = false;
                updateToggleUI();
            }
        });

        hostingRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHostingRide = true;
                updateToggleUI();
            }
        });

        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        timeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        createRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHostingRide) {
                    createRide();
                } else {
                    searchRides();
                }
            }
        });
    }

    private void updateToggleUI() {
        if (isHostingRide) {
            hostingRideButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            hostingRideButton.setTextColor(getColor(R.color.white));
            lookingForRideButton.setBackgroundTintList(getColorStateList(R.color.status_inactive));
            lookingForRideButton.setTextColor(getColor(R.color.primary_blue));
            createRideButton.setText("Create Ride");
        } else {
            lookingForRideButton.setBackgroundTintList(getColorStateList(R.color.status_active));
            lookingForRideButton.setTextColor(getColor(R.color.white));
            hostingRideButton.setBackgroundTintList(getColorStateList(R.color.status_inactive));
            hostingRideButton.setTextColor(getColor(R.color.primary_blue));
            createRideButton.setText("Search Rides");
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_create);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                try {
                    Intent intent = new Intent(CreateRideActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } catch (Exception e) {
                    Toast.makeText(CreateRideActivity.this, "Error opening Home", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return false;
                }
            } else if (itemId == R.id.nav_create) {
                return true;
            } else if (itemId == R.id.nav_messages) {
                try {
                    Intent intent = new Intent(CreateRideActivity.this, MessagesActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } catch (Exception e) {
                    Toast.makeText(CreateRideActivity.this, "Error opening Messages", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return false;
                }
            } else if (itemId == R.id.nav_profile) {
                try {
                    Intent intent = new Intent(CreateRideActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } catch (Exception e) {
                    Toast.makeText(CreateRideActivity.this, "Error opening Profile", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    selectedDate = dateFormat.format(selectedCalendar.getTime());
                    dateText.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    selectedTime = timeFormat.format(selectedCalendar.getTime());
                    timeText.setText(selectedTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void searchRides() {
        String from = fromEditText.getText().toString().trim();
        String to = toEditText.getText().toString().trim();

        if (from.isEmpty() || to.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please fill in From, To, and Date", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, SearchRideActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("to", to);
        intent.putExtra("date", selectedDate);
        startActivity(intent);
    }

    private void createRide() {
        String from = fromEditText.getText().toString().trim();
        String to = toEditText.getText().toString().trim();
        String seatsStr = seatsEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();

        // Validation
        if (from.isEmpty()) {
            fromEditText.setError("Required");
            return;
        }
        if (to.isEmpty()) {
            toEditText.setError("Required");
            return;
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }
        if (seatsStr.isEmpty()) {
            seatsEditText.setError("Required");
            return;
        }
        if (priceStr.isEmpty()) {
            priceEditText.setError("Required");
            return;
        }

        int seats = Integer.parseInt(seatsStr);
        double price = Double.parseDouble(priceStr);

        if (seats <= 0) {
            seatsEditText.setError("Must be greater than 0");
            return;
        }
        if (price <= 0) {
            priceEditText.setError("Must be greater than 0");
            return;
        }

        // Create ride
        String userId = prefsHelper.getUserId();
        String userName = prefsHelper.getUserName();

        String rideType = isHostingRide ? "hosting" : "looking";
        Ride ride = new Ride(
                userId, userName, from, to,
                0.0, 0.0, 0.0, 0.0,
                selectedDate, selectedTime, seats, price, rideType
        );

        // NEW: Set amenities
        ride.setAllowsLuggage(luggageCheckBox.isChecked());
        ride.setAllowsPets(petsCheckBox.isChecked());
        ride.setAllowsBikes(bikesCheckBox.isChecked());
        ride.setAllowsSnowboards(snowboardsCheckBox.isChecked());

        createRideButton.setEnabled(false);
        createRideButton.setText("Creating...");

        String rideId = firebaseHelper.getRidesRef().push().getKey();
        if (rideId != null) {
            ride.setRideId(rideId);
            firebaseHelper.getRideRef(rideId).setValue(ride)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateRideActivity.this, "Ride created successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(CreateRideActivity.this, RideConfirmationActivity.class);
                        intent.putExtra("confirmationType", "ride_created");
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        createRideButton.setEnabled(true);
                        createRideButton.setText("Create Ride");
                        Toast.makeText(CreateRideActivity.this, "Failed to create ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_create);
        }
    }
}