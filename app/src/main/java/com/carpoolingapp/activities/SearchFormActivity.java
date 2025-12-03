package com.carpoolingapp.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.carpoolingapp.R;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SearchFormActivity extends AppCompatActivity {

    private EditText fromEditText, toEditText;
    private TextView dateText;
    private MaterialButton searchButton;
    private View dateLayout;
    private ImageView swapButton;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_form);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        fromEditText = findViewById(R.id.fromEditText);
        toEditText = findViewById(R.id.toEditText);
        dateText = findViewById(R.id.dateText);
        searchButton = findViewById(R.id.searchButton);
        dateLayout = findViewById(R.id.dateLayout);
        swapButton = findViewById(R.id.imageView4);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Plan your ride");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // Date picker
        dateLayout.setOnClickListener(v -> showDatePicker());

        // Search button
        searchButton.setOnClickListener(v -> performSearch());

        // Swap button - FIXED!
        if (swapButton != null) {
            swapButton.setOnClickListener(v -> swapLocations());
        }
    }

    private void swapLocations() {
        // Get current values
        String fromText = fromEditText.getText().toString().trim();
        String toText = toEditText.getText().toString().trim();

        // Swap them
        fromEditText.setText(toText);
        toEditText.setText(fromText);

        // Show feedback
        Toast.makeText(this, "Locations swapped!", Toast.LENGTH_SHORT).show();
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

    private void performSearch() {
        String from = fromEditText.getText().toString().trim();
        String to = toEditText.getText().toString().trim();

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

        // Navigate to search results
        Intent intent = new Intent(this, SearchRideActivity.class);
        intent.putExtra("from", from);
        intent.putExtra("to", to);
        intent.putExtra("date", selectedDate);
        startActivity(intent);
    }
}