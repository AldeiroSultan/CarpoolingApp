package com.carpoolingapp.activities;

// File: CarpoolingApp/app/src/main/java/com/carpooling/app/activities/SplashActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.carpoolingapp.R;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUserSession();
            }
        }, SPLASH_DURATION);
    }

    private void checkUserSession() {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);

        Intent intent;
        if (firebaseHelper.isUserLoggedIn() && prefsHelper.isLoggedIn()) {
            // User is logged in, go to Home
            intent = new Intent(this, HomeActivity.class);
        } else {
            // User is not logged in, go to Login
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
