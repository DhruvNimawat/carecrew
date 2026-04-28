package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.ImageView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComplaintDetailsActivity extends AppCompatActivity {

    private String complaintId;

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
        complaintId = getIntent().getStringExtra("id");

        // ... rest of setup ...
        setupViews(category, status, description, block, floor, room, priority, timestampStr, assignedTo);
        loadWorkPhotos();
        checkUserRoleAndStatus(complaintId, status, assignedTo);
    }

    private void setupViews(String category, String status, String description, String block, String floor, String room, String priority, String timestampStr, String assignedTo) {
        String timestamp = "N/A";
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                long timestampLong = Long.parseLong(timestampStr);
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                timestamp = sdf.format(new Date(timestampLong));
            } catch (NumberFormatException e) {
                timestamp = timestampStr;
            }
        }

        ((TextView) findViewById(R.id.detailCategory)).setText(category);
        ((TextView) findViewById(R.id.detailStatus)).setText(status);
        ((TextView) findViewById(R.id.detailDescription)).setText(description);
        ((TextView) findViewById(R.id.detailLocation)).setText(block + ", " + floor + ", " + room);
        ((TextView) findViewById(R.id.detailPriority)).setText(priority);
        ((TextView) findViewById(R.id.detailAssigned)).setText(assignedTo != null && !assignedTo.isEmpty() ? assignedTo : "Not Assigned");
        ((TextView) findViewById(R.id.detailTimestamp)).setText(timestamp);

        View btnViewStaff = findViewById(R.id.btnViewStaffDetails);
        if (assignedTo != null && !assignedTo.isEmpty()) {
            btnViewStaff.setVisibility(View.VISIBLE);
            btnViewStaff.setOnClickListener(v -> showStaffDetailsDialog(assignedTo));
        }
    }

    private void loadWorkPhotos() {
        if (complaintId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("complaints").child(complaintId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint complaint = snapshot.getValue(Complaint.class);
                if (complaint != null) {
                    // Update status in case it changed while viewing
                    TextView tvStatus = findViewById(R.id.detailStatus);
                    if (tvStatus != null) {
                        tvStatus.setText(complaint.status);
                    }

                    View layoutWorkPhotos = findViewById(R.id.layoutWorkPhotos);
                    ImageView ivBefore = findViewById(R.id.ivBeforeDetail);
                    ImageView ivAfter = findViewById(R.id.ivAfterDetail);
                    View layoutBefore = findViewById(R.id.layoutBeforePhoto);
                    View layoutAfter = findViewById(R.id.layoutAfterPhoto);

                    boolean hasPhotos = false;
                    boolean isVisibleStatus = "Started".equalsIgnoreCase(complaint.status) 
                            || "In Progress".equalsIgnoreCase(complaint.status) 
                            || "Completed".equalsIgnoreCase(complaint.status) 
                            || "Resolved".equalsIgnoreCase(complaint.status);

                    // Before Photo Logic: Prioritize Staff's 'Before' image, fallback to User's image
                    if (complaint.startWorkImageUrl != null && !complaint.startWorkImageUrl.isEmpty()) {
                        layoutBefore.setVisibility(View.VISIBLE);
                        Glide.with(ComplaintDetailsActivity.this).load(complaint.startWorkImageUrl).into(ivBefore);
                        hasPhotos = true;
                        ivBefore.setOnClickListener(v -> showImageDialog(complaint.startWorkImageUrl, "Before Repair (Staff Capture)"));
                    } else if (complaint.imageUrl != null && !complaint.imageUrl.isEmpty()) {
                        layoutBefore.setVisibility(View.VISIBLE);
                        Glide.with(ComplaintDetailsActivity.this).load(complaint.imageUrl).into(ivBefore);
                        hasPhotos = true;
                        ivBefore.setOnClickListener(v -> showImageDialog(complaint.imageUrl, "Before Repair (User Capture)"));
                    } else {
                        layoutBefore.setVisibility(View.GONE);
                    }

                    // After Photo Logic
                    if (complaint.afterRepairImageUrl != null && !complaint.afterRepairImageUrl.isEmpty()) {
                        layoutAfter.setVisibility(View.VISIBLE);
                        Glide.with(ComplaintDetailsActivity.this).load(complaint.afterRepairImageUrl).into(ivAfter);
                        hasPhotos = true;
                        ivAfter.setOnClickListener(v -> showImageDialog(complaint.afterRepairImageUrl, "After Repair"));
                    } else {
                        layoutAfter.setVisibility(View.GONE);
                    }

                    // Visibility Logic: Show section if photos exist OR if status implies work has started/finished
                    if (hasPhotos || isVisibleStatus) {
                        layoutWorkPhotos.setVisibility(View.VISIBLE);
                    } else {
                        layoutWorkPhotos.setVisibility(View.GONE);
                    }

                    // Update review button visibility if status changed to Completed
                    View btnReview = findViewById(R.id.btnSubmitReviewDetails);
                    if (btnReview != null) {
                        if ("Completed".equalsIgnoreCase(complaint.status) || "Resolved".equalsIgnoreCase(complaint.status)) {
                            btnReview.setVisibility(View.VISIBLE);
                            // Ensure listener is set even if status changed in real-time
                            btnReview.setOnClickListener(v -> showReviewDialog(complaint.id, complaint.assignedTo, complaint.userId));
                        } else {
                            btnReview.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showImageDialog(String imageUrl, String title) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        ImageView imageView = new ImageView(this);
        imageView.setPadding(16, 16, 16, 16);
        Glide.with(this).load(imageUrl).into(imageView);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(imageView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void checkUserRoleAndStatus(String complaintId, String status, String assignedTo) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserEmail = user != null ? user.getEmail() : null;
        String userId = getIntent().getStringExtra("userId");

        if (currentUserEmail != null) {
            String emailKey = currentUserEmail.replace(".", ",");
            FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String role = snapshot.child("role").getValue(String.class);
                            if ("User".equalsIgnoreCase(role)) {
                                if (userId != null && !userId.equals(emailKey)) {
                                    View btnJoin = findViewById(R.id.btnJoinComplaint);
                                    if (btnJoin != null && "Pending".equalsIgnoreCase(status)) {
                                        btnJoin.setVisibility(View.VISIBLE);
                                        btnJoin.setOnClickListener(v -> joinComplaint(complaintId, emailKey));
                                    }
                                }
                            } else if ("Admin".equalsIgnoreCase(role)) {
                                View btnAllocate = findViewById(R.id.btnAllocateStaff);
                                if (btnAllocate != null && ("Pending".equalsIgnoreCase(status) || "Unassigned".equalsIgnoreCase(status))) {
                                    btnAllocate.setVisibility(View.VISIBLE);
                                    btnAllocate.setOnClickListener(v -> showAllocationDialog(complaintId));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        View btnReview = findViewById(R.id.btnSubmitReviewDetails);
        if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) {
            btnReview.setVisibility(View.VISIBLE);
            btnReview.setOnClickListener(v -> showReviewDialog(complaintId, assignedTo, userId));
        } else {
            btnReview.setVisibility(View.GONE);
            // Even if not visible now, we can pre-set the listener or let the real-time listener handle it
            btnReview.setOnClickListener(v -> showReviewDialog(complaintId, assignedTo, userId));
        }
    }

    private void joinComplaint(String complaintId, String userEmailKey) {
        if (complaintId == null) return;
        DatabaseReference joinedRef = FirebaseDatabase.getInstance().getReference()
                .child("complaints").child(complaintId).child("joinedUsers");
        
        joinedRef.child(userEmailKey).setValue(true).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "You have joined this complaint!", Toast.LENGTH_SHORT).show();
            findViewById(R.id.btnJoinComplaint).setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to join complaint.", Toast.LENGTH_SHORT).show();
        });
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
            findViewById(R.id.btnViewStaffDetails).setVisibility(View.VISIBLE);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Allocation failed.", Toast.LENGTH_SHORT).show();
        });
    }

    private void showStaffDetailsDialog(String staffEmail) {
        if (staffEmail == null || staffEmail.isEmpty()) return;
        
        String emailKey = staffEmail.replace(".", ",");
        FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String category = snapshot.child("category").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            
                            StringBuilder sb = new StringBuilder();
                            sb.append("Name: ").append(name != null ? name : "N/A").append("\n");
                            sb.append("Category: ").append(category != null ? category : "N/A").append("\n");
                            sb.append("Email: ").append(staffEmail).append("\n");
                            if (phone != null && !phone.isEmpty()) {
                                sb.append("Phone: ").append(phone);
                            }
                            
                            new AlertDialog.Builder(ComplaintDetailsActivity.this)
                                    .setTitle("Staff Information")
                                    .setMessage(sb.toString())
                                    .setPositiveButton("Close", null)
                                    .show();
                        } else {
                            Toast.makeText(ComplaintDetailsActivity.this, "Staff details not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });
    }
}