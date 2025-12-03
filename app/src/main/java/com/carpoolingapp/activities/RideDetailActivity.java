package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;

public class RideDetailActivity extends AppCompatActivity {

    private TextView fromText, toText, dateText, timeText, priceText, seatsText, driverNameText;
    private MaterialButton bookNowButton;

    private SharedPrefsHelper prefsHelper;
    private String rideId;
    private String driverId;
    private String driverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        prefsHelper = new SharedPrefsHelper(this);

        initViews();
        setupToolbar();
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
        bookNowButton = findViewById(R.id.bookNowButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ride Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadRideData() {
        boolean isDemo = getIntent().getBooleanExtra("isDemo", false);

        if (isDemo) {
            loadDemoData();
            return;
        }

        // Load actual ride data
        rideId = getIntent().getStringExtra("rideId");
        driverId = getIntent().getStringExtra("driverId");
        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        double price = getIntent().getDoubleExtra("price", 0.0);
        int seats = getIntent().getIntExtra("seats", 0);
        driverName = getIntent().getStringExtra("driverName");

        if (from != null) fromText.setText(from);
        if (to != null) toText.setText(to);
        if (date != null) dateText.setText(date);
        if (time != null) timeText.setText(time);
        priceText.setText("$" + String.format("%.2f", price));
        seatsText.setText(seats + " seats available");
        if (driverName != null) driverNameText.setText(driverName);
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
    }

    private void setupListeners() {
        bookNowButton.setOnClickListener(v -> bookRide());
    }

    private void bookRide() {
        // Check if user is trying to book their own ride
        String currentUserId = prefsHelper.getUserId();

        if (currentUserId != null && currentUserId.equals(driverId)) {
            Toast.makeText(this, "You cannot book your own ride!", Toast.LENGTH_LONG).show();
            return;
        }

        double price = getIntent().getDoubleExtra("price", 15.00);
        boolean isDemo = getIntent().getBooleanExtra("isDemo", false);

        // Go to payment
        Intent intent = new Intent(RideDetailActivity.this, PaymentActivity.class);
        intent.putExtra("totalPrice", price);
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
}