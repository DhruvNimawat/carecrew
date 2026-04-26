package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StaffDashboard extends AppCompatActivity {

    private TextView tvAcceptedCount, tvPendingCount, tvStaffGreeting;
    private TextView tvCompletedTodayCount, tvUrgentCount;
    private ImageButton btnMenu;
    private View actionAvailableTickets, actionMyJobs, actionHistory, navProfileGrid;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View headerView;
    private TextView tvDrawerName, tvDrawerEmail, tvDrawerRole, tvUserID;
    private View layoutProfileDetails;
    private boolean isProfileExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        // Entrance Animation
        View mainLayout = findViewById(R.id.drawerLayout);
        if (mainLayout != null) {
            mainLayout.setAlpha(0f);
            mainLayout.animate().alpha(1f).setDuration(500).start();
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        headerView = navigationView.getHeaderView(0);

        // Initialize Stats Views
        tvAcceptedCount = findViewById(R.id.tvAcceptedCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvCompletedTodayCount = findViewById(R.id.tvCompletedTodayCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        tvStaffGreeting = findViewById(R.id.tvStaffGreeting);

        // Initialize Buttons
        btnMenu = findViewById(R.id.btnMenu);
        actionAvailableTickets = findViewById(R.id.actionAvailableTickets);
        actionMyJobs = findViewById(R.id.actionMyJobs);
        navProfileGrid = findViewById(R.id.navProfileGrid);

        // Completed Today / History
        actionHistory = findViewById(R.id.cardCompletedToday);

        setupClickListeners();
        setupNavigationDrawer();
        setupStatsListeners();
        fetchStaffDetails();
    }

    private void fetchStaffDetails() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "unknown";

            tvUserID = headerView.findViewById(R.id.tvUserID);
            if (tvUserID != null) tvUserID.setText(getString(R.string.user_id_label_format, email));

            tvDrawerEmail = headerView.findViewById(R.id.tvDrawerStaffEmail);
            if (tvDrawerEmail != null) tvDrawerEmail.setText(getString(R.string.email_label_format, email));

            mDatabase.child("Users").child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String category = snapshot.child("category").getValue(String.class);

                        if (name != null && !name.isEmpty()) {
                            String displayName = name.substring(0, 1).toUpperCase() + name.substring(1);
                            tvStaffGreeting.setText(getString(R.string.welcome_user_format, displayName.split(" ")[0]));
                            
                            tvDrawerName = headerView.findViewById(R.id.tvDrawerStaffName);
                            if (tvDrawerName != null) tvDrawerName.setText(name);
                        }

                        if (category != null) {
                            tvDrawerRole = headerView.findViewById(R.id.tvDrawerStaffRole);
                            if (tvDrawerRole != null) tvDrawerRole.setText(getString(R.string.role_label_format, category));
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupClickListeners() {
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
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

    private void setupNavigationDrawer() {
        layoutProfileDetails = headerView.findViewById(R.id.layoutProfileDetails);
        MaterialButton btnViewProfile = headerView.findViewById(R.id.btnViewProfile);
        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(v -> {
                isProfileExpanded = !isProfileExpanded;
                layoutProfileDetails.setVisibility(isProfileExpanded ? View.VISIBLE : View.GONE);
                btnViewProfile.setText(isProfileExpanded ? "Hide Full Profile" : "View Full Profile");
                btnViewProfile.setIconResource(isProfileExpanded ? R.drawable.ic_back : R.drawable.ic_chevron_right);
            });
        }

        // App Info Dropdown Logic
        MaterialButton btnViewAppInfo = headerView.findViewById(R.id.btnViewAppInfo);
        View layoutAppInfoDetails = headerView.findViewById(R.id.layoutAppInfoDetails);
        if (btnViewAppInfo != null) {
            btnViewAppInfo.setOnClickListener(v -> {
                boolean isVisible = layoutAppInfoDetails.getVisibility() == View.VISIBLE;
                layoutAppInfoDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                btnViewAppInfo.setIconResource(isVisible ? R.drawable.ic_chevron_right : R.drawable.ic_back);
            });
        }

        // Help Centre Dropdown Logic
        MaterialButton btnViewHelp = headerView.findViewById(R.id.btnViewHelp);
        View layoutHelpDetails = headerView.findViewById(R.id.layoutHelpDetails);
        if (btnViewHelp != null) {
            btnViewHelp.setOnClickListener(v -> {
                boolean isVisible = layoutHelpDetails.getVisibility() == View.VISIBLE;
                layoutHelpDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                btnViewHelp.setIconResource(isVisible ? R.drawable.ic_chevron_right : R.drawable.ic_back);
            });
        }

        // Terms Dropdown Logic
        MaterialButton btnViewTerms = headerView.findViewById(R.id.btnViewTerms);
        View layoutTermsDetails = headerView.findViewById(R.id.layoutTermsDetails);
        if (btnViewTerms != null) {
            btnViewTerms.setOnClickListener(v -> {
                boolean isVisible = layoutTermsDetails.getVisibility() == View.VISIBLE;
                layoutTermsDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                btnViewTerms.setIconResource(isVisible ? R.drawable.ic_chevron_right : R.drawable.ic_back);
            });
        }

        // Rate Us Button Logic
        View btnRateUs = headerView.findViewById(R.id.btnRateUs);
        if (btnRateUs != null) {
            btnRateUs.setOnClickListener(v -> {
                Toast.makeText(this, "Rate Us feature coming soon!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }

        View btnDrawerLogout = headerView.findViewById(R.id.btnDrawerLogout);
        if (btnDrawerLogout != null) {
            btnDrawerLogout.setOnClickListener(v -> logout());
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(StaffDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
                                long completionTime = 0;
                                if (ticket.completionTimeMillis instanceof Long) {
                                    completionTime = (Long) ticket.completionTimeMillis;
                                } else if (ticket.completionTimeMillis instanceof String) {
                                    try {
                                        completionTime = Long.parseLong((String) ticket.completionTimeMillis);
                                    } catch (Exception ignored) {}
                                }

                                if (completionTime >= todayStart) {
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