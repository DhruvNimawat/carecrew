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

public class StaffDashboard extends AppCompatActivity {

    private TextView tvAcceptedCount, tvPendingCount, tvStaffGreeting;
    private TextView tvCompletedTodayCount, tvUrgentCount;
    private ImageButton btnLogout;
    private View actionAvailableTickets, actionMyJobs, actionHistory, navProfileGrid;
    private View navHome, navHistory, navProfile;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        // Entrance Animation
        View mainLayout = findViewById(R.id.main);
        mainLayout.setAlpha(0f);
        mainLayout.animate().alpha(1f).setDuration(500).start();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Stats Views
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvCompletedTodayCount = findViewById(R.id.tvCompletedTodayCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        tvStaffGreeting = findViewById(R.id.tvStaffGreeting);

        // Initialize Buttons
        btnLogout = findViewById(R.id.btnLogout);
        actionAvailableTickets = findViewById(R.id.actionAvailableTickets);
        actionMyJobs = findViewById(R.id.actionMyJobs);
        navProfileGrid = findViewById(R.id.navProfileGrid);

        // Completed Today / History
        actionHistory = findViewById(R.id.cardCompletedToday);

        // Initialize Nav (Removed as per UI update)
        // navHome = findViewById(R.id.navHome);
        // navHistory = findViewById(R.id.navHistory);
        // navProfile = findViewById(R.id.navProfile);

        setupClickListeners();
        setupStatsListeners();
        fetchStaffName();
    }

    private void fetchStaffName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "unknown";
            mDatabase.child("Users").child(emailKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        }
                        tvStaffGreeting.setText(getString(R.string.welcome_name_format, name));
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
            Intent intent = new Intent(StaffDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        actionAvailableTickets.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboard.this, AvailableTicketsActivity.class));
        });

        actionMyJobs.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboard.this, AcceptedJobsActivity.class));
        });

        actionHistory.setOnClickListener(v -> {
            startActivity(new Intent(StaffDashboard.this, StaffHistoryActivity.class));
        });

        navProfileGrid.setOnClickListener(v -> {
            openProfile();
        });
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("role", "Staff");
        startActivity(intent);
    }

    private void setupStatsListeners() {
        if (mAuth.getCurrentUser() == null) return;
        String staffEmail = mAuth.getCurrentUser().getEmail();
        String emailKey = staffEmail != null ? staffEmail.replace(".", ",") : "unknown";

        mDatabase.child("Users").child(emailKey).child("category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String staffCategory = userSnapshot.getValue(String.class);

                mDatabase.child("complaints").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int acceptedCount = 0;
                        int availableCount = 0;
                        int completedToday = 0;
                        int urgentMyJobs = 0;

                        long todayStart = getStartOfToday();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Complaint ticket = ds.getValue(Complaint.class);
                            if (ticket == null) continue;

                            String status = ticket.status;
                            String priority = ticket.priority;
                            String assignedTo = ticket.assignedTo;
                            String ticketCategory = ticket.category;

                            boolean isAssignedToMe = staffEmail != null && staffEmail.equalsIgnoreCase(assignedTo);
                            boolean isUrgent = "Urgent".equalsIgnoreCase(priority) || "High".equalsIgnoreCase(priority);
                            boolean isRejectedByMe = ds.child("rejectedBy").hasChild(emailKey);
                            boolean isUnassigned = (assignedTo == null || assignedTo.isEmpty());

                            // 1. My Current Active Jobs
                            if (isAssignedToMe && ("Assigned".equalsIgnoreCase(status) || "Started".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status))) {
                                acceptedCount++;
                                if (isUrgent) urgentMyJobs++;
                            }
                            // 2. Available Jobs (Unassigned, Open/Pending/Cancelled, In my category, Not rejected)
                            else if (isUnassigned && ("Open".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) && !isRejectedByMe) {
                                if (staffCategory != null && staffCategory.equalsIgnoreCase(ticketCategory)) {
                                    availableCount++;
                                }
                            }
                            // 3. Completed Today
                            else if (isAssignedToMe && ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status))) {
                                if (ticket.completionTimeMillis >= todayStart) {
                                    completedToday++;
                                }
                            }
                        }

                        tvAcceptedCount.setText(String.valueOf(acceptedCount));
                        tvPendingCount.setText(String.valueOf(availableCount));
                        tvCompletedTodayCount.setText(String.valueOf(completedToday));
                        tvUrgentCount.setText(String.valueOf(urgentMyJobs));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private long getStartOfToday() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}