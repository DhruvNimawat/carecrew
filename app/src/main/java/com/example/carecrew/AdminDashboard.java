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

public class AdminDashboard extends AppCompatActivity {

    private TextView tvTotalUsers, tvOpenTickets, tvActiveStaff, tvSystemAlerts, tvAdminGreeting;
    private ImageButton btnLogout;
    private View actionAddStaff, actionTicketsCenter, actionStaffReviews, actionAnnouncements;
    private View navHome, navRaise, navComplaints, navProfile;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

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
        
        // Initialize Buttons
        btnLogout = findViewById(R.id.btnLogout);
        actionAddStaff = findViewById(R.id.actionAddStaff);
        actionTicketsCenter = findViewById(R.id.actionTicketsCenter);
        actionStaffReviews = findViewById(R.id.actionStaffReviews);
        actionAnnouncements = findViewById(R.id.actionAnnouncements);

        // Initialize Nav
        navHome = findViewById(R.id.navHome);
        navRaise = findViewById(R.id.navRaise);
        navComplaints = findViewById(R.id.navComplaints);
        navProfile = findViewById(R.id.navProfile);

        setupClickListeners();
        setupStatsListeners();
        fetchAdminName();
    }

    private void fetchAdminName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null) {
                String emailKey = email.replace(".", ",");
                FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fullName = snapshot.getValue(String.class);
                            if (fullName != null && !fullName.isEmpty()) {
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

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

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
            intent.putExtra("role", "Admin");
            startActivity(intent);
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