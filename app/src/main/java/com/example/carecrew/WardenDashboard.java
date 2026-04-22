package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

public class WardenDashboard extends AppCompatActivity {

    private TextView tvActiveIssues, tvPendingApprovals, tvStaffOnDuty, tvResolvedToday, tvWardenGreeting;
    private ImageButton btnLogout;
    private View actionComplaints, actionInspections, actionStaff, actionEmergency;
    private View navHome, navRaise, navComplaints, navProfile;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Stats Views
        tvActiveIssues = findViewById(R.id.tvActiveIssues);
        tvPendingApprovals = findViewById(R.id.tvPendingApprovals);
        tvStaffOnDuty = findViewById(R.id.tvStaffOnDuty);
        tvResolvedToday = findViewById(R.id.tvResolvedToday);
        tvWardenGreeting = findViewById(R.id.tvWardenGreeting);

        // Initialize Action Cards
        actionComplaints = findViewById(R.id.actionComplaints);
        actionInspections = findViewById(R.id.actionInspections);
        actionStaff = findViewById(R.id.actionStaff);
        actionEmergency = findViewById(R.id.actionEmergency);

        // Initialize Nav
        btnLogout = findViewById(R.id.btnLogout);
        navHome = findViewById(R.id.navHome);
        navRaise = findViewById(R.id.navRaise);
        navComplaints = findViewById(R.id.navComplaints);
        navProfile = findViewById(R.id.navProfile);

        setupClickListeners();
        setupStatsListeners();
        fetchWardenName();
    }

    private void fetchWardenName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "unknown";
            mDatabase.child("Users").child(emailKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        tvWardenGreeting.setText("Welcome, " + name);
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
            Intent intent = new Intent(WardenDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        actionComplaints.setOnClickListener(v -> {
            startActivity(new Intent(WardenDashboard.this, TicketCenterActivity.class));
        });

        actionInspections.setOnClickListener(v -> {
            Toast.makeText(this, "Room Inspections coming soon!", Toast.LENGTH_SHORT).show();
        });

        actionStaff.setOnClickListener(v -> {
            Toast.makeText(this, "Staff Management coming soon!", Toast.LENGTH_SHORT).show();
        });

        actionEmergency.setOnClickListener(v -> {
            Toast.makeText(this, "Emergency Protocol initiated!", Toast.LENGTH_LONG).show();
        });

        // Bottom Nav Listeners
        navHome.setOnClickListener(v -> {
            // Already here
        });
        navRaise.setOnClickListener(v -> {
            startActivity(new Intent(this, RaiseTicketActivity.class));
        });
        navComplaints.setOnClickListener(v -> {
            startActivity(new Intent(this, TicketCenterActivity.class));
        });
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", "Warden");
            startActivity(intent);
        });
    }

    private void setupStatsListeners() {
        // Active Issues (Tickets with status Open/Pending)
        mDatabase.child("complaints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int activeCount = 0;
                int resolvedCount = 0;
                for (DataSnapshot ticket : snapshot.getChildren()) {
                    String status = ticket.child("status").getValue(String.class);
                    if ("Open".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status)) {
                        activeCount++;
                    } else if ("Resolved".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                        resolvedCount++;
                    }
                }
                tvActiveIssues.setText(String.valueOf(activeCount));
                tvResolvedToday.setText(String.valueOf(resolvedCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Staff on Duty count
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long staffCount = 0;
                for (DataSnapshot user : snapshot.getChildren()) {
                    String role = user.child("role").getValue(String.class);
                    if ("staff".equalsIgnoreCase(role)) staffCount++;
                }
                tvStaffOnDuty.setText(String.valueOf(staffCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Pending Approvals (Placeholder)
        tvPendingApprovals.setText("3");
    }
}