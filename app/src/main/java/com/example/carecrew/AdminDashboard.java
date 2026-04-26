package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboard extends AppCompatActivity {

    private TextView tvTotalUsers, tvOpenTickets, tvActiveStaff, tvSystemAlerts, tvAdminGreeting;
    private View actionAddStaff, actionTicketsCenter, actionStaffReviews, actionAnnouncements;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View headerView;
    private TextView tvDrawerAdminName, tvDrawerAdminEmail, tvUserID;
    private View layoutProfileDetails;
    private boolean isProfileExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Stats Views
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvOpenTickets = findViewById(R.id.tvOpenTickets);
        tvActiveStaff = findViewById(R.id.tvActiveStaff);
        tvSystemAlerts = findViewById(R.id.tvSystemAlerts);
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        headerView = navigationView.getHeaderView(0);

        tvDrawerAdminName = headerView.findViewById(R.id.tvDrawerAdminName);
        tvDrawerAdminEmail = headerView.findViewById(R.id.tvDrawerAdminEmail);
        tvUserID = headerView.findViewById(R.id.tvUserID);
        layoutProfileDetails = headerView.findViewById(R.id.layoutProfileDetails);

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        }

        View btnDrawerLogout = headerView.findViewById(R.id.btnDrawerLogout);
        if (btnDrawerLogout != null) {
            btnDrawerLogout.setOnClickListener(v -> logout());
        }

        setupNavigationDrawer();
        
        // Initialize Buttons
        actionAddStaff = findViewById(R.id.actionAddStaff);
        actionTicketsCenter = findViewById(R.id.actionTicketsCenter);
        actionStaffReviews = findViewById(R.id.actionStaffReviews);
        actionAnnouncements = findViewById(R.id.actionAnnouncements);

        setupClickListeners();
        setupStatsListeners();
        fetchAdminName();
    }

    private void setupNavigationDrawer() {
        // Profile Section Expansion
        View profileSection = headerView.findViewById(R.id.profileSection);
        MaterialButton btnViewProfile = headerView.findViewById(R.id.btnViewProfile);
        
        View.OnClickListener profileToggleListener = v -> {
            isProfileExpanded = !isProfileExpanded;
            if (layoutProfileDetails != null) {
                layoutProfileDetails.setVisibility(isProfileExpanded ? View.VISIBLE : View.GONE);
            }
            if (btnViewProfile != null) {
                btnViewProfile.setIconResource(isProfileExpanded ? R.drawable.ic_back : R.drawable.ic_chevron_right);
                btnViewProfile.setText(isProfileExpanded ? "Hide Full Profile" : "View Full Profile");
            }
        };

        if (profileSection != null) {
            profileSection.setOnClickListener(profileToggleListener);
        }
        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(profileToggleListener);
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
    }

    private void fetchAdminName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            if (tvUserID != null) tvUserID.setText(getString(R.string.user_id_format, email));
            if (tvDrawerAdminEmail != null) tvDrawerAdminEmail.setText(getString(R.string.email_format, email));

            if (email != null) {
                String emailKey = email.replace(".", ",");
                FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fullName = snapshot.child("name").getValue(String.class);
                            if (fullName != null && !fullName.isEmpty()) {
                                if (tvDrawerAdminName != null) tvDrawerAdminName.setText(getString(R.string.name_format, fullName));
                                String firstName = fullName.split(" ")[0];
                                tvAdminGreeting.setText(getString(R.string.welcome_user_format, firstName));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupClickListeners() {
        actionAddStaff.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, AddUserActivity.class));
        });

        actionTicketsCenter.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, TicketCenterActivity.class));
        });

        actionStaffReviews.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, StaffReviewsActivity.class));
        });

        actionAnnouncements.setOnClickListener(v -> {
            startActivity(new Intent(AdminDashboard.this, CreateAnnouncementActivity.class));
        });
    }

    private void setupStatsListeners() {
        // Total Users
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvTotalUsers.setText(String.valueOf(snapshot.getChildrenCount()));
                
                // Active Staff count (example: users with role 'staff')
                long staffCount = 0;
                for (DataSnapshot user : snapshot.getChildren()) {
                    String role = user.child("role").getValue(String.class);
                    if ("staff".equalsIgnoreCase(role)) staffCount++;
                }
                tvActiveStaff.setText(String.valueOf(staffCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Open Tickets
        mDatabase.child("Tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int openCount = 0;
                for (DataSnapshot ticket : snapshot.getChildren()) {
                    String status = ticket.child("status").getValue(String.class);
                    if ("Open".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
                        openCount++;
                    }
                }
                tvOpenTickets.setText(String.valueOf(openCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}