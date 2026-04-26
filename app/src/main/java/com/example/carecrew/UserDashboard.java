package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashboard extends AppCompatActivity {

    private TextView tvTotalTickets, tvPendingCount, tvInProgressCount, tvUrgentCount, tvWelcomeTitle;
    private ImageView btnLogout;
    private View cardRaiseComplaint, cardMyTickets, cardUpdates, cardProfile;
    private View navHome, navHistory, navProfile;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        tvTotalTickets = findViewById(R.id.userLocation); // Using existing ID from layout
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        tvWelcomeTitle = findViewById(R.id.welcomeTitle);

        btnLogout = findViewById(R.id.btnLogout);
        cardRaiseComplaint = findViewById(R.id.cardRaiseComplaint);
        cardMyTickets = findViewById(R.id.cardMyTickets);
        cardUpdates = findViewById(R.id.cardUpdates);
        cardProfile = findViewById(R.id.cardProfile);

        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navProfile = findViewById(R.id.navProfile);

        setupClickListeners();
        setupStatsListeners();
        fetchUserName();
    }

    private void fetchUserName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "unknown";
            mDatabase.child("Users").child(emailKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        tvWelcomeTitle.setText(getString(R.string.welcome_name_format, name));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(UserDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        cardRaiseComplaint.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboard.this, RaiseTicketActivity.class));
        });

        cardMyTickets.setOnClickListener(v -> {
            startActivity(new Intent(UserDashboard.this, MyTicketsActivity.class));
        });

        cardUpdates.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_recent_updates), Toast.LENGTH_SHORT).show();
        });

        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", "User");
            startActivity(intent);
        });

        // Bottom Nav Listeners
        navHome.setOnClickListener(v -> {
            // Already here
        });
        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, TicketCenterActivity.class));
        });
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", "User");
            startActivity(intent);
        });
    }

    private void setupStatsListeners() {
        if (mAuth.getCurrentUser() == null) return;
        String userEmail = mAuth.getCurrentUser().getEmail();

        mDatabase.child("complaints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int pending = 0;
                int inProgress = 0;
                int urgent = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint ticket = ds.getValue(Complaint.class);
                    if (ticket != null && userEmail != null && userEmail.equalsIgnoreCase(ticket.userId)) {
                        total++;
                        String status = ticket.status;
                        String priority = ticket.priority;

                        if ("Open".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
                            pending++;
                        } else if ("Assigned".equalsIgnoreCase(status) || "Started".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status)) {
                            inProgress++;
                        }

                        if ("Urgent".equalsIgnoreCase(priority) || "High".equalsIgnoreCase(priority)) {
                            urgent++;
                        }
                    }
                }

                tvTotalTickets.setText(String.valueOf(total));
                tvPendingCount.setText(String.valueOf(pending));
                tvInProgressCount.setText(String.valueOf(inProgress));
                tvUrgentCount.setText(String.valueOf(urgent));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}