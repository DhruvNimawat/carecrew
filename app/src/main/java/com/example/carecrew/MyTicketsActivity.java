package com.example.carecrew;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import android.widget.RatingBar;
import com.google.android.material.textfield.TextInputEditText;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView ticketsRecyclerView;
    private TicketAdapter ticketAdapter;
    private List<Complaint> allTickets;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private MaterialButton btnAll, btnPending, btnInProgress, btnCompleted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        allTickets = new ArrayList<>();
        ticketAdapter = new TicketAdapter(allTickets, this::showReviewDialog);
        ticketsRecyclerView.setAdapter(ticketAdapter);

        setupFilterButtons();
        fetchTickets();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void showReviewDialog(Complaint complaint) {
        if (!"Completed".equalsIgnoreCase(complaint.status) && !"Resolved".equalsIgnoreCase(complaint.status)) {
            // If not completed, maybe show details instead (original behavior)
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_review, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.reviewRatingBar);
        TextInputEditText etComment = dialogView.findViewById(R.id.etReviewComment);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = etComment.getText().toString();
                    submitReview(complaint, rating, comment);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitReview(Complaint complaint, float rating, String comment) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference().child("Reviews").push();
        
        java.util.Map<String, Object> reviewData = new java.util.HashMap<>();
        reviewData.put("complaintId", complaint.id);
        reviewData.put("userId", complaint.userId);
        reviewData.put("assignedTo", complaint.assignedTo);
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("timestamp", System.currentTimeMillis());

        reviewRef.setValue(reviewData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Review submitted! Thank you.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilterButtons() {
        btnAll = findViewById(R.id.tabAll);
        btnPending = findViewById(R.id.tabPending);
        btnInProgress = findViewById(R.id.tabInProgress);
        btnCompleted = findViewById(R.id.tabCompleted);

        btnAll.setOnClickListener(v -> filterTickets("All"));
        btnPending.setOnClickListener(v -> filterTickets("Pending"));
        btnInProgress.setOnClickListener(v -> filterTickets("In Progress"));
        btnCompleted.setOnClickListener(v -> filterTickets("Completed"));
    }

    private void fetchTickets() {
        String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;
        String currentUserId = email != null ? email.replace(".", ",") : "anonymous";
        
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTickets.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Complaint ticket = postSnapshot.getValue(Complaint.class);
                    if (ticket != null && currentUserId.equals(ticket.userId)) {
                        allTickets.add(ticket);
                    }
                }
                ticketAdapter.updateList(allTickets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyTicketsActivity.this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTickets(String status) {
        // Reset button styles (simplified for now)
        resetButtonStyles();
        
        List<Complaint> filteredList = new ArrayList<>();
        if ("All".equals(status)) {
            filteredList = allTickets;
            btnAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            btnAll.setTextColor(getResources().getColor(R.color.dark_blue));
        } else {
            for (Complaint t : allTickets) {
                if (status.equalsIgnoreCase(t.status)) {
                    filteredList.add(t);
                }
            }
            // Update active button color
            if ("Pending".equals(status)) {
                btnPending.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                btnPending.setTextColor(getResources().getColor(R.color.dark_blue));
            } else if ("In Progress".equals(status)) {
                btnInProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                btnInProgress.setTextColor(getResources().getColor(R.color.dark_blue));
            } else if ("Completed".equals(status)) {
                btnCompleted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
                btnCompleted.setTextColor(getResources().getColor(R.color.dark_blue));
            }
        }
        ticketAdapter.updateList(filteredList);
    }

    private void resetButtonStyles() {
        MaterialButton[] buttons = {btnAll, btnPending, btnInProgress, btnCompleted};
        for (MaterialButton b : buttons) {
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            b.setTextColor(android.graphics.Color.parseColor("#CCFFFFFF"));
        }
    }
}