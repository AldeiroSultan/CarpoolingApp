package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.carpoolingapp.models.Booking;
import com.carpoolingapp.models.Ride;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class PaymentActivity extends AppCompatActivity {

    private TextInputEditText cardNumberEditText, expiryEditText, cvvEditText;
    private TextView totalPriceText;
    private MaterialButton makePaymentButton;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;
    private RadioButton creditRadio;
    private RadioButton debitRadio;
    private MaterialCardView creditCard;
    private MaterialCardView debitCard;

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
        creditRadio = findViewById(R.id.creditCardRadio);
        debitRadio = findViewById(R.id.debitCardRadio);

        creditCard = findViewById(R.id.creditCardOption);
        debitCard = findViewById(R.id.debitCardOption);
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

        View.OnClickListener selectCredit = v -> updatePaymentSelection(true);
        View.OnClickListener selectDebit = v -> updatePaymentSelection(false);

        creditRadio.setOnClickListener(selectCredit);
        debitRadio.setOnClickListener(selectDebit);
        creditCard.setOnClickListener(selectCredit);
        debitCard.setOnClickListener(selectDebit);

        // Default selection
        updatePaymentSelection(true);
    }
    private void loadPaymentInfo() {
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        totalPriceText.setText("$" + String.format("%.2f", totalPrice));
    }

    private void updatePaymentSelection(boolean creditSelected) {
        creditRadio.setChecked(creditSelected);
        debitRadio.setChecked(!creditSelected);

        int activeStroke = getColor(R.color.status_active);
        int inactiveStroke = getColor(R.color.status_inactive);

        creditCard.setStrokeColor(creditSelected ? activeStroke : inactiveStroke);
        debitCard.setStrokeColor(!creditSelected ? activeStroke : inactiveStroke);
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

        // Simulate payment processing
        makePaymentButton.setEnabled(false);
        makePaymentButton.setText("Processing...");

        // Simulate success after delay
        makePaymentButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_SHORT).show();

                // Get payment details from intent
                String rideId = getIntent().getStringExtra("rideId");
                String driverId = getIntent().getStringExtra("driverId");
                String driverName = getIntent().getStringExtra("driverName");
                String fromLocation = getIntent().getStringExtra("from");
                String toLocation = getIntent().getStringExtra("to");
                String date = getIntent().getStringExtra("date");
                String time = getIntent().getStringExtra("time");
                int seats = getIntent().getIntExtra("seatsBooked", 1);
                double totalAmount = getIntent().getDoubleExtra("totalPrice", 1.0);

                if (isDemo) {
                    // Demo flow: Show receipt then go to chat
                    Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
                    intent.putExtra("bookingId", "DEMO123");
                    intent.putExtra("rideId", rideId);
                    intent.putExtra("riderName", "Demo Rider");
                    intent.putExtra("riderEmail", "rider@demo.com");
                    intent.putExtra("driverId", driverId);
                    intent.putExtra("driverName", driverName != null ? driverName : "Demo Driver");
                    intent.putExtra("driverEmail", "driver@demo.com");
                    intent.putExtra("fromLocation", fromLocation != null ? fromLocation : "Downtown Vancouver");
                    intent.putExtra("toLocation", toLocation != null ? toLocation : "Surrey Central");
                    intent.putExtra("date", date != null ? date : "Dec 15, 2024");
                    intent.putExtra("time", time != null ? time : "10:00 AM");
                    intent.putExtra("seats", seats);
                    intent.putExtra("pricePerSeat", totalAmount / seats);
                    intent.putExtra("totalAmount", totalAmount);
                    intent.putExtra("paymentMethod", "Demo Payment");
                    intent.putExtra("isDemo", true);
                    startActivity(intent);
                    finish();
                } else {
                    // Normal flow: create booking record before showing receipt
                    createBooking();

                    // Normal flow: Create receipt and show it
                    // TODO: Save receipt to Firebase
                    Intent intent = new Intent(PaymentActivity.this, ReceiptActivity.class);
                    intent.putExtra("bookingId", rideId + driverId);
                    intent.putExtra("rideId", rideId);
                    intent.putExtra("riderId", prefsHelper.getUserId());
                    intent.putExtra("riderName", prefsHelper.getUserName());
                    intent.putExtra("riderEmail", prefsHelper.getUserEmail());
                    intent.putExtra("driverId", driverId);
                    intent.putExtra("driverName", driverName);
                    intent.putExtra("fromLocation", fromLocation);
                    intent.putExtra("toLocation", toLocation);
                    intent.putExtra("date", date);
                    intent.putExtra("time", time);
                    intent.putExtra("seats", seats);
                    intent.putExtra("pricePerSeat", totalAmount / seats);
                    intent.putExtra("totalAmount", totalAmount);
                    intent.putExtra("paymentMethod", "Credit Card");
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    private void createBooking() {
        String rideId = getIntent().getStringExtra("rideId");
        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        double totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);
        int seatsBooked = getIntent().getIntExtra("seatsBooked", 1);
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
                seatsBooked, // seats booked
                totalPrice,
                "Credit Card"
        );

        // Save to Firebase
        String bookingId = firebaseHelper.getBookingsRef().push().getKey();
        if (bookingId != null) {
            booking.setBookingId(bookingId);
            firebaseHelper.getBookingRef(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        // Booking saved successfully – update ride seats
                        updateRideSeats(rideId, seatsBooked);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PaymentActivity.this,
                                "Failed to save booking: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateRideSeats(String rideId, int seatsBooked) {
        if (rideId == null || seatsBooked <= 0) return;

        firebaseHelper.getRideRef(rideId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                if (ride == null) return;

                int available = ride.getAvailableSeats();
                available = Math.max(0, available - seatsBooked);
                ride.setAvailableSeats(available);

                firebaseHelper.getRideRef(rideId).setValue(ride);
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                // Ignore seat update failures for now
            }
        });
    }
}