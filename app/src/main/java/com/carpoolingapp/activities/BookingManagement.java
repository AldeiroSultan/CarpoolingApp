// File: CarpoolingApp/app/src/main/java/com/carpooling/app/activities/BookingManagementActivity.java

package com.carpoolingapp.activities;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.carpoolingapp.R;

public class BookingManagementActivity extends AppCompatActivity {

    private RecyclerView bookingsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_management);

        initViews();
        setupToolbar();
    }

    private void initViews() {
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView);
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
}