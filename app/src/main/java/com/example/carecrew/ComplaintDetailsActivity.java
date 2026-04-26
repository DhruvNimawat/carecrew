package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                timestamp = sdf.format(new Date(timestampLong));
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
        ((TextView) findViewById(R.id.detailAssigned)).setText(assignedTo != null && !assignedTo.isEmpty() ? assignedTo : "Not Assigned");
        ((TextView) findViewById(R.id.detailTimestamp)).setText(timestamp);

        // Handle Review Button for Completed tickets
        View btnReview = findViewById(R.id.btnSubmitReviewDetails);
        if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
            btnReview.setVisibility(View.VISIBLE);
            btnReview.setOnClickListener(v -> showReviewDialog(getIntent().getStringExtra("id"), assignedTo, getIntent().getStringExtra("userId")));
        } else {
            btnReview.setVisibility(View.GONE);
        }
    }

    private void showReviewDialog(String complaintId, String assignedTo, String userId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.reviewRatingBar);
        TextInputEditText etComment = dialogView.findViewById(R.id.etReviewComment);

        new AlertDialog.Builder(this)
                .setTitle("Rate Service")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString();
                    submitReview(complaintId, assignedTo, userId, rating, comment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitReview(String complaintId, String assignedTo, String userId, float rating, String comment) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference().child("Reviews").push();

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("complaintId", complaintId);
        reviewData.put("userId", userId);
        reviewData.put("assignedTo", assignedTo);
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("timestamp", System.currentTimeMillis());

        reviewRef.setValue(reviewData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnSubmitReviewDetails).setVisibility(View.GONE); // Hide after submission
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
        });
    }
}