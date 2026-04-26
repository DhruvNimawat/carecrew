package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WardenDashboard extends AppCompatActivity {

    private RecyclerView rvWardenTickets;
    private WardenTicketAdapter adapter;
    private List<Complaint> complaintList;
    private List<Complaint> fullList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String wardenHostel = "";
    
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialButton btnFilterPending, btnFilterUrgent, btnFilterResolved, btnFilterAll;
    private View headerView;
    private TextView tvDrawerName, tvDrawerEmail, tvDrawerHostel, tvUserID;
    private View layoutProfileDetails;
    private boolean isProfileExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        
        // Since we are using a custom layout inside NavigationView, we access views differently
        // If the entire layout is the header, we use getHeaderView(0)
        headerView = navigationView.getHeaderView(0);
        
        tvDrawerName = headerView.findViewById(R.id.tvDrawerWardenName);
        tvDrawerEmail = headerView.findViewById(R.id.tvDrawerWardenEmail);
        tvDrawerHostel = headerView.findViewById(R.id.tvDrawerWardenHostel);
        tvUserID = headerView.findViewById(R.id.tvUserID);
        layoutProfileDetails = headerView.findViewById(R.id.layoutProfileDetails);
        View layoutAppInfoDetails = headerView.findViewById(R.id.layoutAppInfoDetails);

        com.google.android.material.button.MaterialButton btnViewProfile = headerView.findViewById(R.id.btnViewProfile);
        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(v -> {
                isProfileExpanded = !isProfileExpanded;
                layoutProfileDetails.setVisibility(isProfileExpanded ? View.VISIBLE : View.GONE);
                btnViewProfile.setText(isProfileExpanded ? "Hide Full Profile" : "View Full Profile");
                btnViewProfile.setIconResource(isProfileExpanded ? R.drawable.ic_back : R.drawable.ic_chevron_right);
            });
        }

        // App Info Dropdown Logic
        com.google.android.material.button.MaterialButton btnViewAppInfo = headerView.findViewById(R.id.btnViewAppInfo);
        if (btnViewAppInfo != null) {
            btnViewAppInfo.setOnClickListener(v -> {
                boolean isVisible = layoutAppInfoDetails.getVisibility() == View.VISIBLE;
                layoutAppInfoDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                btnViewAppInfo.setIconResource(isVisible ? R.drawable.ic_chevron_right : R.drawable.ic_back);
            });
        }

        // Help Centre Dropdown Logic
        com.google.android.material.button.MaterialButton btnViewHelp = headerView.findViewById(R.id.btnViewHelp);
        View layoutHelpDetails = headerView.findViewById(R.id.layoutHelpDetails);
        if (btnViewHelp != null) {
            btnViewHelp.setOnClickListener(v -> {
                boolean isVisible = layoutHelpDetails.getVisibility() == View.VISIBLE;
                layoutHelpDetails.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                btnViewHelp.setIconResource(isVisible ? R.drawable.ic_chevron_right : R.drawable.ic_back);
            });
        }

        // Terms Dropdown Logic
        com.google.android.material.button.MaterialButton btnViewTerms = headerView.findViewById(R.id.btnViewTerms);
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
                showRateUsDialog();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }

        rvWardenTickets = findViewById(R.id.rvWardenTickets);
        rvWardenTickets.setLayoutManager(new LinearLayoutManager(this));
        
        complaintList = new ArrayList<>();
        fullList = new ArrayList<>();
        adapter = new WardenTicketAdapter(complaintList, complaint -> {
            Intent intent = new Intent(WardenDashboard.this, ComplaintDetailsActivity.class);
            intent.putExtra("category", complaint.category);
            intent.putExtra("status", complaint.status);
            intent.putExtra("description", complaint.description);
            intent.putExtra("block", complaint.block);
            intent.putExtra("floor", complaint.floor);
            intent.putExtra("roomNumber", complaint.roomNumber);
            intent.putExtra("priority", complaint.priority);
            intent.putExtra("timestamp", complaint.getTimestampString());
            intent.putExtra("assignedTo", complaint.assignedTo);
            intent.putExtra("userId", complaint.userId);
            startActivity(intent);
        });
        rvWardenTickets.setAdapter(adapter);

        btnFilterPending = findViewById(R.id.btnFilterPending);
        btnFilterUrgent = findViewById(R.id.btnFilterUrgent);
        btnFilterResolved = findViewById(R.id.btnFilterResolved);
        btnFilterAll = findViewById(R.id.btnFilterAll);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        }

        View btnDrawerLogout = headerView.findViewById(R.id.btnDrawerLogout);
        if (btnDrawerLogout != null) {
            btnDrawerLogout.setOnClickListener(v -> logout());
        }

        setupNavigationDrawer();
        fetchWardenHostelAndComplaints();
        setupFilters();
    }

    private void setupNavigationDrawer() {
        // App Info Dropdown Logic
        View layoutAppInfoDetails = headerView.findViewById(R.id.layoutAppInfoDetails);
        MaterialButton btnViewAppInfo = headerView.findViewById(R.id.btnViewAppInfo);
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
                showRateUsDialog();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }

        // Profile Section Expansion
        View profileSection = headerView.findViewById(R.id.profileSection);
        if (profileSection != null) {
            profileSection.setOnClickListener(v -> {
                isProfileExpanded = !isProfileExpanded;
                if (layoutProfileDetails != null) {
                    layoutProfileDetails.setVisibility(isProfileExpanded ? View.VISIBLE : View.GONE);
                }
                MaterialButton btnViewProfile = headerView.findViewById(R.id.btnViewProfile);
                if (btnViewProfile != null) {
                    btnViewProfile.setIconResource(isProfileExpanded ? R.drawable.ic_back : R.drawable.ic_chevron_right);
                }
            });
        }
    }

    private void showAppDetailsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Settings - App Details")
            .setMessage("App Name: CareCrew\nVersion: 1.0.4\nDetails: A centralized complaint management system for campus hostels.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void showRateUsDialog() {
        View view = LayoutInflater.from(this).inflate(android.R.layout.select_dialog_item, null);
        Toast.makeText(this, "Rate Us feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showFAQDialog() {
        String faqText = "Q: What is the purpose of this application?\n" +
                "A: This application enables authorized campus users to report, track, and manage maintenance-related issues efficiently.\n\n" +
                "Q: Who can use this application?\n" +
                "A: The application is intended for students, faculty, staff, and other authorized personnel within the campus.\n\n" +
                "Q: How do I submit a maintenance request?\n" +
                "A: Users can submit a request by selecting the relevant category, providing a description of the issue, and submitting it through the app interface.\n\n" +
                "Q: How can I track the status of my request?\n" +
                "A: Users can view the progress and status updates of their submitted requests within the “My Requests” or similar section of the application.\n\n" +
                "Q: How long does it take to resolve an issue?\n" +
                "A: Resolution times may vary depending on the nature, complexity, and priority of the request. The application does not guarantee immediate resolution.\n\n" +
                "Q: What should I do if I submitted incorrect information?\n" +
                "A: Users should update or resubmit the request with accurate information, or contact the support team if editing is not available.\n\n" +
                "Q: Is my personal information secure?\n" +
                "A: The application implements reasonable security measures to protect user data. For more details, please refer to the Privacy Policy.\n\n" +
                "Q: Can I report multiple issues at once?\n" +
                "A: Users are encouraged to submit separate requests for each issue to ensure proper tracking and resolution.\n\n" +
                "Q: What happens if I misuse the application?\n" +
                "A: Misuse, including false reporting or inappropriate behavior, may result in restriction or termination of access.\n\n" +
                "Q: Who do I contact for support or technical issues?\n" +
                "A: Users can contact the designated support team through the “Support” section within the application.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Frequently Asked Questions (FAQs)")
                .setMessage(faqText)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showTermsDialog() {
        String termsText = "1. Use of Service\nThis app is intended for students, staff, and authorized personnel to report and track campus maintenance issues.\n\n" +
                "2. User Responsibility\nUsers must provide accurate and genuine information while submitting maintenance requests.\n\n" +
                "3. Prohibited Activities\nMisuse of the app, including false complaints, spam, or abusive content, is strictly prohibited.\n\n" +
                "4. Request Handling\nAll maintenance requests will be reviewed and processed based on priority and availability of resources.\n\n" +
                "5. No Guarantee of Immediate Service\nThe app does not guarantee instant resolution of reported issues.\n\n" +
                "6. Account Security\nUsers are responsible for maintaining the confidentiality of their login credentials.\n\n" +
                "7. Data Usage\nThe app may collect basic user data to improve services and track maintenance requests.\n\n" +
                "8. Modification of Services\nThe app administrators reserve the right to update or modify features without prior notice.\n\n" +
                "9. Limitation of Liability\nThe app is not liable for delays, damages, or losses caused by maintenance issues or service interruptions.\n\n" +
                "10. Termination of Access\nThe app may suspend or terminate access for users who violate these terms.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Terms & Conditions")
                .setMessage(termsText)
                .setPositiveButton("Close", null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(WardenDashboard.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchWardenHostelAndComplaints() {
        if (mAuth.getCurrentUser() == null) return;
        
        String email = mAuth.getCurrentUser().getEmail();
        String emailKey = email != null ? email.replace(".", ",") : "unknown";
        
        if (tvUserID != null) tvUserID.setText("ID: " + email);
        
        FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    if (userSnapshot.exists()) {
                        String name = userSnapshot.child("name").getValue(String.class);
                        wardenHostel = userSnapshot.child("hostel").getValue(String.class);
                        if (wardenHostel == null) {
                            wardenHostel = userSnapshot.child("block").getValue(String.class);
                        }
                        
                        if (name != null) {
                            if (tvDrawerName != null) tvDrawerName.setText(name);
                            TextView tvWelcome = findViewById(R.id.tvWardenWelcome);
                            if (tvWelcome != null) {
                                String firstName = name.split(" ")[0];
                                tvWelcome.setText(getString(R.string.welcome_user_format, firstName));
                            }
                        }
                        if (tvDrawerEmail != null) tvDrawerEmail.setText("Email: " + email);
                        if (tvDrawerHostel != null) tvDrawerHostel.setText("Hostel: " + wardenHostel);

                        loadComplaints();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    private void loadComplaints() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Complaint complaint = data.getValue(Complaint.class);
                    if (complaint != null) {
                        // More robust hostel matching
                        boolean hostelMatch = false;
                        if (wardenHostel == null || wardenHostel.isEmpty()) {
                            hostelMatch = true;
                        } else if (complaint.block != null) {
                            String block = complaint.block.toLowerCase();
                            String wardenH = wardenHostel.toLowerCase();
                            hostelMatch = block.contains(wardenH) || wardenH.contains(block);
                        }
                        
                        if (hostelMatch) {
                            fullList.add(complaint);
                        }
                    }
                }
                filter("All");
                updateHeaderCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WardenDashboard.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHeaderCount() {
        TextView tvTitle = findViewById(R.id.tvWardenTitle);
        if (tvTitle != null) {
            tvTitle.setText("Hostel Complaints");
        }
    }

    private void setupFilters() {
        if (btnFilterPending != null) btnFilterPending.setOnClickListener(v -> filter("Pending"));
        if (btnFilterUrgent != null) btnFilterUrgent.setOnClickListener(v -> filter("Urgent"));
        if (btnFilterResolved != null) btnFilterResolved.setOnClickListener(v -> filter("Resolved"));
        if (btnFilterAll != null) btnFilterAll.setOnClickListener(v -> filter("All"));
    }

    private void filter(String status) {
        updateButtonStyle(btnFilterAll, false);
        updateButtonStyle(btnFilterPending, false);
        updateButtonStyle(btnFilterUrgent, false);
        updateButtonStyle(btnFilterResolved, false);

        if (status.equalsIgnoreCase("All")) updateButtonStyle(btnFilterAll, true);
        else if (status.equalsIgnoreCase("Pending")) updateButtonStyle(btnFilterPending, true);
        else if (status.equalsIgnoreCase("Urgent")) updateButtonStyle(btnFilterUrgent, true);
        else if (status.equalsIgnoreCase("Resolved")) updateButtonStyle(btnFilterResolved, true);

        complaintList.clear();
        if (status.equalsIgnoreCase("All")) {
            complaintList.addAll(fullList);
        } else if (status.equalsIgnoreCase("Urgent")) {
            for (Complaint c : fullList) {
                if ("Urgent".equalsIgnoreCase(c.priority) || "High".equalsIgnoreCase(c.priority)) {
                    complaintList.add(c);
                }
            }
        } else {
            for (Complaint c : fullList) {
                if (status.equalsIgnoreCase(c.status)) {
                    complaintList.add(c);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateButtonStyle(com.google.android.material.button.MaterialButton button, boolean isSelected) {
        if (button == null) return;
        if (isSelected) {
            button.setBackgroundColor(getResources().getColor(R.color.role_warden));
            button.setTextColor(getResources().getColor(R.color.white));
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            button.setTextColor(getResources().getColor(R.color.text_desc));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.bg_blue_medium)));
            button.setStrokeWidth(2);
        }
    }

    private static class WardenTicketAdapter extends RecyclerView.Adapter<WardenTicketAdapter.ViewHolder> {
        private List<Complaint> list;
        private OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(Complaint complaint);
        }

        WardenTicketAdapter(List<Complaint> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket_warden, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Complaint c = list.get(position);
            
            if (c.userId != null) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(c.userId)
                    .child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                holder.tvStudentName.setText(snapshot.getValue(String.class));
                            } else {
                                holder.tvStudentName.setText("Unknown");
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
            } else {
                holder.tvStudentName.setText("N/A");
            }

            holder.tvCategory.setText(c.category);
            holder.tvRoom.setText(c.roomNumber);
            holder.tvStaff.setText(c.assignedTo != null ? c.assignedTo : "None");
            holder.tvStatus.setText(c.status);
            
            holder.itemView.setOnClickListener(v -> listener.onItemClick(c));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStudentName, tvCategory, tvRoom, tvStaff, tvStatus;
            ViewHolder(View v) {
                super(v);
                tvStudentName = v.findViewById(R.id.tvStudentName);
                tvCategory = v.findViewById(R.id.tvCategory);
                tvRoom = v.findViewById(R.id.tvRoom);
                tvStaff = v.findViewById(R.id.tvStaff);
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }
    }
}
