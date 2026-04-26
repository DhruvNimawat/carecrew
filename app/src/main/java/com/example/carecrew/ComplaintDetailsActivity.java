package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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

        // Check if admin is logged in
        String currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (currentUserEmail != null) {
            String emailKey = currentUserEmail.replace(".", ",");
            FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey).child("role")
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            String role = snapshot.getValue(String.class);
                            if ("Admin".equalsIgnoreCase(role)) {
                                View btnAllocate = findViewById(R.id.btnAllocateStaff);
                                if (btnAllocate != null) {
                                    btnAllocate.setVisibility(View.VISIBLE);
                                    btnAllocate.setOnClickListener(v -> showAllocationDialog(getIntent().getStringExtra("id")));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                    });
        }

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

    private void showAllocationDialog(String complaintId) {
        FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("role").equalTo("Staff")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        List<String> staffNames = new ArrayList<>();
                        List<String> staffEmails = new ArrayList<>();
                        for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                            String name = ds.child("name").getValue(String.class);
                            String email = ds.child("email").getValue(String.class);
                            if (name != null && email != null) {
                                staffNames.add(name + " (" + email + ")");
                                staffEmails.add(email);
                            }
                        }

                        if (staffNames.isEmpty()) {
                            Toast.makeText(ComplaintDetailsActivity.this, "No staff members found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] namesArray = staffNames.toArray(new String[0]);
                        new AlertDialog.Builder(ComplaintDetailsActivity.this)
                                .setTitle("Select Staff to Allocate")
                                .setItems(namesArray, (dialog, which) -> {
                                    allocateStaff(complaintId, staffEmails.get(which), namesArray[which].split(" \\(")[0]);
                                })
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });
    }

    private void allocateStaff(String complaintId, String staffEmail, String staffName) {
        if (complaintId == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("complaints").child(complaintId);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("assignedTo", staffEmail);
        updates.put("staffName", staffName);
        updates.put("status", "Assigned");

        ref.updateChildren(updates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Staff allocated successfully!", Toast.LENGTH_SHORT).show();
            ((TextView) findViewById(R.id.detailAssigned)).setText(staffEmail);
            ((TextView) findViewById(R.id.detailStatus)).setText("Assigned");
            findViewById(R.id.btnAllocateStaff).setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Allocation failed.", Toast.LENGTH_SHORT).show();
        });
    }
}