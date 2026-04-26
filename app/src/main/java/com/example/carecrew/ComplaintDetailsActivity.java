package com.example.carecrew;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ComplaintDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        String category = getIntent().getStringExtra("category");
        String status = getIntent().getStringExtra("status");
        String description = getIntent().getStringExtra("description");
        String block = getIntent().getStringExtra("block");
        String floor = getIntent().getStringExtra("floor");
        String room = getIntent().getStringExtra("roomNumber");
        String priority = getIntent().getStringExtra("priority");
        String timestampStr = getIntent().getStringExtra("timestamp");
        String assignedTo = getIntent().getStringExtra("assignedTo");

        String timestamp = "N/A";
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                long timestampLong = Long.parseLong(timestampStr);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
                timestamp = sdf.format(new java.util.Date(timestampLong));
            } catch (NumberFormatException e) {
                timestamp = timestampStr; // If it's already a formatted string
            }
        }

        // Set data to views
        ((TextView) findViewById(R.id.detailCategory)).setText(category);
        ((TextView) findViewById(R.id.detailStatus)).setText(status);
        ((TextView) findViewById(R.id.detailDescription)).setText(description);
        ((TextView) findViewById(R.id.detailLocation)).setText(block + ", " + floor + ", " + room);
        ((TextView) findViewById(R.id.detailPriority)).setText(priority);
        ((TextView) findViewById(R.id.detailAssigned)).setText(assignedTo != null ? assignedTo : "Not Assigned");
        ((TextView) findViewById(R.id.detailTimestamp)).setText(timestamp);
    }
}