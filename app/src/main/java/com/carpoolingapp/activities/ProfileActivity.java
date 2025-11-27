// File: CarpoolingApp/app/src/main/java/com/carpooling/app/activities/ProfileActivity.java

package com.carpoolingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private TextView userNameText, userEmailText, ratingText;
    private TextView editProfileOption, changePasswordOption, paymentMethodsOption, rideHistoryOption;
    private MaterialButton logoutButton;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        initFirebase();
        setupToolbar();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        ratingText = findViewById(R.id.ratingText);
        editProfileOption = findViewById(R.id.editProfileOption);
        changePasswordOption = findViewById(R.id.changePasswordOption);
        paymentMethodsOption = findViewById(R.id.paymentMethodsOption);
        rideHistoryOption = findViewById(R.id.rideHistoryOption);
        logoutButton = findViewById(R.id.logoutButton);
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
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadUserData() {
        userNameText.setText(prefsHelper.getUserName());
        userEmailText.setText(prefsHelper.getUserEmail());
    }

    private void setupListeners() {
        editProfileOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Edit Profile coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        changePasswordOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Change Password coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        paymentMethodsOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Payment Methods coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        rideHistoryOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Ride History coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });
    }

    private void performLogout() {
        firebaseHelper.getAuth().signOut();
        prefsHelper.clearUserData();

        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}