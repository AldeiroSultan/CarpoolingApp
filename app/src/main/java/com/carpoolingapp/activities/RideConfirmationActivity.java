package com.carpoolingapp.activities;

// File: CarpoolingApp/app/src/main/java/com/carpooling/app/activities/RideConfirmationActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.carpoolingapp.R;
import com.google.android.material.button.MaterialButton;

public class RideConfirmationActivity extends AppCompatActivity {

    private LinearLayout rootLayout;
    private TextView confirmationTitle, confirmationSubtitle;
    private MaterialButton primaryButton, secondaryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_confirmation);

        initViews();
        setupConfirmation();
        setupListeners();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        confirmationTitle = findViewById(R.id.confirmationTitle);
        confirmationSubtitle = findViewById(R.id.confirmationSubtitle);
        primaryButton = findViewById(R.id.primaryButton);
        secondaryButton = findViewById(R.id.secondaryButton);
    }

    private void setupConfirmation() {
        String type = getIntent().getStringExtra("confirmationType");

        if (type == null) type = "booking";

        switch (type) {
            case "booking":
                rootLayout.setBackgroundColor(getColor(R.color.primary_blue));
                confirmationTitle.setText(R.string.ride_confirmed);
                confirmationSubtitle.setText(R.string.find_your_ride);
                primaryButton.setText(R.string.view_your_booking);
                secondaryButton.setText(R.string.back_to_home);
                break;

            case "ride_created":
                rootLayout.setBackgroundColor(getColor(R.color.primary_blue));
                confirmationTitle.setText(R.string.ride_created);
                confirmationSubtitle.setText("Your ride is now listed");
                primaryButton.setText(R.string.save_to_listing);
                secondaryButton.setText(R.string.back_to_home);
                break;

            case "booking_cancelled":
                rootLayout.setBackgroundColor(getColor(R.color.primary_pink));
                confirmationTitle.setText(R.string.booking_cancelled);
                confirmationSubtitle.setText("Your booking has been cancelled");
                primaryButton.setText(R.string.back_to_home);
                secondaryButton.setVisibility(View.GONE);
                break;

            case "listing_removed":
                rootLayout.setBackgroundColor(getColor(R.color.primary_pink));
                confirmationTitle.setText(R.string.listing_removed);
                confirmationSubtitle.setText("Your listing has been removed");
                primaryButton.setText(R.string.back_to_home);
                secondaryButton.setVisibility(View.GONE);
                break;

            case "ride_completed":
                rootLayout.setBackgroundColor(getColor(R.color.primary_green));
                confirmationTitle.setText(R.string.ride_completed);
                confirmationSubtitle.setText(R.string.find_your_rider);
                primaryButton.setText("Rate Rider");
                secondaryButton.setText(R.string.back_to_home);
                break;

            case "drive_completed":
                rootLayout.setBackgroundColor(getColor(R.color.primary_green));
                confirmationTitle.setText(R.string.drive_completed);
                confirmationSubtitle.setText("Thank you for the ride!");
                primaryButton.setText("Rate Driver");
                secondaryButton.setText(R.string.back_to_home);
                break;
        }
    }

    private void setupListeners() {
        primaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate based on confirmation type
                Intent intent = new Intent(RideConfirmationActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        secondaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RideConfirmationActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}