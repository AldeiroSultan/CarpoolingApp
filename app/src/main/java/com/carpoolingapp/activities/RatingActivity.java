package com.carpoolingapp.activities;

// File: CarpoolingApp/app/src/main/java/com/carpooling/app/activities/RatingActivity.java
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RatingActivity extends AppCompatActivity {

    private TextView ratingTitle;
    private RatingBar ratingBar;
    private TextInputEditText reviewEditText;
    private MaterialButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        ratingTitle = findViewById(R.id.ratingTitle);
        ratingBar = findViewById(R.id.ratingBar);
        reviewEditText = findViewById(R.id.reviewEditText);
        submitButton = findViewById(R.id.submitButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupListeners() {
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRating();
            }
        });
    }

    private void submitRating() {
        float rating = ratingBar.getRating();
        String review = reviewEditText.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        // TODO: Save rating to Firebase

        // Simulate success
        submitButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RatingActivity.this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, 1500);
    }
}