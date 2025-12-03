package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.carpoolingapp.models.Booking;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PaymentActivity extends AppCompatActivity {

    private TextInputEditText cardNumberEditText, expiryEditText, cvvEditText;
    private TextView totalPriceText;
    private MaterialButton makePaymentButton;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);

        initViews();
        setupToolbar();
        setupListeners();
        loadPaymentInfo();
    }

    private void initViews() {
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        expiryEditText = findViewById(R.id.expiryEditText);
        cvvEditText = findViewById(R.id.cvvEditText);
        totalPriceText = findViewById(R.id.totalPriceText);
        makePaymentButton = findViewById(R.id.makePaymentButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        makePaymentButton.setOnClickListener(v -> processPayment());
    }

    private void loadPaymentInfo() {
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        totalPriceText.setText("$" + String.format("%.2f", totalPrice));
    }

    private void processPayment() {
        String cardNumber = cardNumberEditText.getText().toString().trim();
        String expiry = expiryEditText.getText().toString().trim();
        String cvv = cvvEditText.getText().toString().trim();

        boolean isDemo = getIntent().getBooleanExtra("isDemo", false);

        if (!isDemo && (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty())) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDemo && cardNumber.length() < 13) {
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show();
            return;
        }

        makePaymentButton.setEnabled(false);
        makePaymentButton.setText("Processing...");

        // Create booking in Firebase
        createBooking();

        makePaymentButton.postDelayed(() -> {
            Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_SHORT).show();

            // Go to chat
            Intent intent = new Intent(PaymentActivity.this, ChatActivity.class);
            intent.putExtra("isDemo", isDemo);
            intent.putExtra("rideId", getIntent().getStringExtra("rideId"));
            intent.putExtra("from", getIntent().getStringExtra("from"));
            intent.putExtra("to", getIntent().getStringExtra("to"));
            intent.putExtra("date", getIntent().getStringExtra("date"));
            intent.putExtra("time", getIntent().getStringExtra("time"));
            intent.putExtra("otherUserName", getIntent().getStringExtra("driverName"));

            startActivity(intent);
            finish();
        }, 2000);
    }

    private void createBooking() {
        String rideId = getIntent().getStringExtra("rideId");
        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        String driverId = getIntent().getStringExtra("driverId");
        String driverName = getIntent().getStringExtra("driverName");

        String riderId = prefsHelper.getUserId();
        String riderName = prefsHelper.getUserName();

        if (riderId == null || rideId == null) return;

        // Create booking object
        Booking booking = new Booking(
                rideId,
                riderId,
                riderName,
                driverId != null ? driverId : "demo_driver",
                driverName != null ? driverName : "Demo Driver",
                from != null ? from : "Unknown",
                to != null ? to : "Unknown",
                date != null ? date : "TBD",
                time != null ? time : "TBD",
                1, // seats booked
                totalPrice,
                "Credit Card"
        );

        // Save to Firebase
        String bookingId = firebaseHelper.getBookingsRef().push().getKey();
        if (bookingId != null) {
            booking.setBookingId(bookingId);
            firebaseHelper.getBookingRef(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        // Booking saved successfully
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PaymentActivity.this,
                                "Failed to save booking: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}