package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ComplaintDetailsActivity extends AppCompatActivity {

    private String ticketId;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get data from intent
        ticketId = getIntent().getStringExtra("id");
        String category = getIntent().getStringExtra("category");
        String status = getIntent().getStringExtra("status");
        String description = getIntent().getStringExtra("description");
        String block = getIntent().getStringExtra("block");
        String floor = getIntent().getStringExtra("floor");
        String room = getIntent().getStringExtra("roomNumber");
        String priority = getIntent().getStringExtra("priority");
        String timestampStr = getIntent().getStringExtra("timestamp");
        String assignedTo = getIntent().getStringExtra("assignedTo");
        float rating = getIntent().getFloatExtra("rating", 0f);
        String review = getIntent().getStringExtra("review");

        String timestamp = "N/A";
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                long timestampLong = Long.parseLong(timestampStr);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
                timestamp = sdf.format(new java.util.Date(timestampLong));
            } catch (NumberFormatException e) {
                timestamp = timestampStr;
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

        setupReviewSection(status, rating, review);
    }

    private void setupReviewSection(String status, float existingRating, String existingReview) {
        View divider = findViewById(R.id.reviewDivider);
        View layoutReview = findViewById(R.id.layoutReview);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextInputLayout tilReview = findViewById(R.id.tilReview);
        TextInputEditText etReview = findViewById(R.id.etReview);
        MaterialButton btnSubmitReview = findViewById(R.id.btnSubmitReview);
        TextView tvSubmittedReview = findViewById(R.id.tvSubmittedReview);

        // Show review section only if status is completed/resolved
        if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
            divider.setVisibility(View.VISIBLE);
            layoutReview.setVisibility(View.VISIBLE);

            if (existingRating > 0) {
                // Review already submitted
                ratingBar.setRating(existingRating);
                ratingBar.setIsIndicator(true);
                tilReview.setVisibility(View.GONE);
                btnSubmitReview.setVisibility(View.GONE);
                tvSubmittedReview.setVisibility(View.VISIBLE);
                tvSubmittedReview.setText(getString(R.string.label_review_prefix) + (existingReview != null ? existingReview : getString(R.string.no_comments)));
            } else {
                // Allow user to submit review
                btnSubmitReview.setOnClickListener(v -> {
                    float rating = ratingBar.getRating();
                    String reviewText = etReview.getText().toString().trim();

                    if (rating == 0) {
                        Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("rating", rating);
                    updates.put("review", reviewText);

                    mDatabase.child("complaints").child(ticketId).updateChildren(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                                ratingBar.setIsIndicator(true);
                                tilReview.setVisibility(View.GONE);
                                btnSubmitReview.setVisibility(View.GONE);
                                tvSubmittedReview.setVisibility(View.VISIBLE);
                                tvSubmittedReview.setText(getString(R.string.label_review_prefix) + (reviewText.isEmpty() ? getString(R.string.no_comments) : reviewText));
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show());
                });
            }
        }
    }
}