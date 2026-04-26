package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private TextView totalTicketsText, tvPendingCount, tvInProgressCount, tvUrgentCount;
    private TextView tvAnnouncementTitle, tvAnnouncementMessage, tvWelcome;
    private TextView tvUserID, tvDrawerAdminName, tvDrawerAdminEmail, tvAdminGreeting;
    private LinearLayout layoutProfileDetails, layoutAppInfoDetails, layoutHelpDetails, layoutTermsDetails;
    private DatabaseReference mDatabase;
    private DatabaseReference mAnnouncementsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");
        mAnnouncementsRef = FirebaseDatabase.getInstance().getReference().child("Announcements");

        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        totalTicketsText = findViewById(R.id.userLocation);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        tvWelcome = findViewById(R.id.welcomeTitle);
        
        tvAnnouncementTitle = findViewById(R.id.tvAnnouncementTitle);
        tvAnnouncementMessage = findViewById(R.id.tvAnnouncementMessage);

        setupDrawer();

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        }

        // Fix for Potential Crashes: Check if views are null
        if (tvAnnouncementTitle == null || tvAnnouncementMessage == null) {
            return;
        }

        CardView cardRaiseComplaint = findViewById(R.id.cardRaiseComplaint);
        CardView cardMyTickets = findViewById(R.id.cardMyTickets);
        CardView cardUpdates = findViewById(R.id.cardUpdates);
        CardView cardProfile = findViewById(R.id.cardProfile);
        
        updateStats();
        listenForAnnouncements();
        fetchUserName();
        applyEntranceAnimations();

        if (cardRaiseComplaint != null) {
            cardRaiseComplaint.setOnClickListener(v -> 
                startActivity(new Intent(UserDashboardActivity.this, RaiseComplaintActivity.class))
            );
        }

        if (cardMyTickets != null) {
            cardMyTickets.setOnClickListener(v -> 
                startActivity(new Intent(UserDashboardActivity.this, MyTicketsActivity.class))
            );
        }

        if (cardUpdates != null) {
            cardUpdates.setOnClickListener(v -> 
                startActivity(new Intent(UserDashboardActivity.this, RecentUpdatesActivity.class))
            );
        }

        if (cardProfile != null) {
            cardProfile.setOnClickListener(v -> 
                startActivity(new Intent(UserDashboardActivity.this, ProfileDetailsActivity.class))
            );
        }
    }

    private void applyEntranceAnimations() {
        View headerBg = findViewById(R.id.headerBackground);
        View welcomeTitle = findViewById(R.id.welcomeTitle);
        View welcomeSubtitle = findViewById(R.id.welcomeSubtitle);
        View statsCard = findViewById(R.id.statsCard);
        View actionGrid = findViewById(R.id.actionGrid);
        View announcementCard = findViewById(R.id.announcementCard);

        // Header slide down
        headerBg.setTranslationY(-200f);
        headerBg.animate().translationY(0).setDuration(800).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();

        // Welcome text fade and slide
        welcomeTitle.setAlpha(0f);
        welcomeTitle.setTranslationX(-50f);
        welcomeTitle.animate().alpha(1f).translationX(0).setDuration(600).setStartDelay(300).start();

        welcomeSubtitle.setAlpha(0f);
        welcomeSubtitle.setTranslationX(-50f);
        welcomeSubtitle.animate().alpha(1f).translationX(0).setDuration(600).setStartDelay(400).start();

        // Stats Card Pop
        statsCard.setAlpha(0f);
        statsCard.setScaleX(0.9f);
        statsCard.setScaleY(0.9f);
        statsCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).setStartDelay(500).setInterpolator(new android.view.animation.OvershootInterpolator()).start();

        // Action Buttons staggered pop
        if (actionGrid instanceof android.view.ViewGroup) {
            android.view.ViewGroup grid = (android.view.ViewGroup) actionGrid;
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                child.setAlpha(0f);
                child.setScaleX(0.8f);
                child.setScaleY(0.8f);
                child.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(700 + (i * 100)).setInterpolator(new android.view.animation.OvershootInterpolator()).start();
            }
        }

        // Announcement Card slide up
        announcementCard.setAlpha(0f);
        announcementCard.setTranslationY(100f);
        announcementCard.animate().alpha(1f).translationY(0).setDuration(700).setStartDelay(1100).start();
    }

    private void setupDrawer() {
        TextView settingsTitle = findViewById(R.id.settingsTitle);
        if (settingsTitle != null) settingsTitle.setText("User Hub");
        
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setBackgroundColor(getResources().getColor(R.color.primary_blue));

        layoutProfileDetails = findViewById(R.id.layoutProfileDetails);
        layoutAppInfoDetails = findViewById(R.id.layoutAppInfoDetails);
        layoutHelpDetails = findViewById(R.id.layoutHelpDetails);
        layoutTermsDetails = findViewById(R.id.layoutTermsDetails);

        tvUserID = findViewById(R.id.tvUserID);
        tvDrawerAdminName = findViewById(R.id.tvDrawerAdminName);
        tvDrawerAdminEmail = findViewById(R.id.tvDrawerAdminEmail);
        tvAdminGreeting = findViewById(R.id.welcomeTitle);

        MaterialButton btnViewProfile = findViewById(R.id.btnViewProfile);
        MaterialButton btnViewAppInfo = findViewById(R.id.btnViewAppInfo);
        MaterialButton btnViewHelp = findViewById(R.id.btnViewHelp);
        MaterialButton btnViewTerms = findViewById(R.id.btnViewTerms);
        MaterialButton btnDrawerLogout = findViewById(R.id.btnDrawerLogout);
        MaterialButton btnRateUs = findViewById(R.id.btnRateUs);

        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(v -> {
                layoutProfileDetails.setVisibility(layoutProfileDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                btnViewProfile.setIconResource(layoutProfileDetails.getVisibility() == View.GONE ? R.drawable.ic_chevron_right : R.drawable.ic_chevron_right);
            });
        }

        if (btnViewAppInfo != null) {
            btnViewAppInfo.setOnClickListener(v -> {
                layoutAppInfoDetails.setVisibility(layoutAppInfoDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                btnViewAppInfo.setIconResource(layoutAppInfoDetails.getVisibility() == View.GONE ? R.drawable.ic_chevron_right : R.drawable.ic_chevron_right);
            });
        }

        if (btnViewHelp != null) {
            btnViewHelp.setOnClickListener(v -> {
                layoutHelpDetails.setVisibility(layoutHelpDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                btnViewHelp.setIconResource(layoutHelpDetails.getVisibility() == View.GONE ? R.drawable.ic_chevron_right : R.drawable.ic_chevron_right);
            });
        }

        if (btnViewTerms != null) {
            btnViewTerms.setOnClickListener(v -> {
                layoutTermsDetails.setVisibility(layoutTermsDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                btnViewTerms.setIconResource(layoutTermsDetails.getVisibility() == View.GONE ? R.drawable.ic_chevron_right : R.drawable.ic_chevron_right);
            });
        }

        if (btnDrawerLogout != null) {
            btnDrawerLogout.setOnClickListener(v -> {
                mAuth.signOut();
                startActivity(new Intent(UserDashboardActivity.this, MainActivity.class));
                finish();
            });
        }

        if (btnRateUs != null) {
            btnRateUs.setOnClickListener(v -> {
                Toast.makeText(this, "Rate Us feature coming soon!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }
    }

    private void fetchUserName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null) {
                String emailKey = email.replace(".", ",");
                FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String fullName = snapshot.child("name").getValue(String.class);
                                    String role = snapshot.child("role").getValue(String.class);
                                    String id = snapshot.child("id").getValue(String.class);

                                    if (fullName != null && !fullName.isEmpty()) {
                                        String firstName = fullName.split(" ")[0];
                                        if (tvWelcome != null) {
                                            tvWelcome.setText(getString(R.string.welcome_user_format, firstName));
                                        }
                                        if (tvDrawerAdminName != null) {
                                            tvDrawerAdminName.setText("Name: " + fullName);
                                        }
                                    }

                                    if (tvDrawerAdminEmail != null) {
                                        tvDrawerAdminEmail.setText("Email: " + email);
                                    }

                                    if (tvUserID != null) {
                                        tvUserID.setText("ID: " + (id != null ? id : "Not Available"));
                                    }

                                    TextView tvRoleLabel = findViewById(R.id.tvRoleLabel);
                                    if (tvRoleLabel != null && role != null) {
                                        tvRoleLabel.setText(role.toUpperCase());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
        }
    }

    private void listenForAnnouncements() {
        mAnnouncementsRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        try {
                            Announcement announcement = postSnapshot.getValue(Announcement.class);
                            if (announcement != null) {
                                tvAnnouncementTitle.setText(announcement.getTitle());
                                tvAnnouncementMessage.setText(announcement.getMessage());
                            }
                        } catch (Exception e) {
                            // Skip if data is in old format
                        }
                    }
                } else {
                    tvAnnouncementTitle.setText(R.string.no_announcements);
                    tvAnnouncementMessage.setText(R.string.check_back_later);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStats() {
        if (mAuth.getCurrentUser() == null) return;
        String email = mAuth.getCurrentUser().getEmail();
        String userId = email != null ? email.replace(".", ",") : "anonymous";
        
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int pending = 0;
                int inProgress = 0;
                int urgent = 0;

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Complaint ticket = postSnapshot.getValue(Complaint.class);
                    if (ticket != null && userId.equals(ticket.userId)) {
                        total++;
                        
                        String status = ticket.status != null ? ticket.status : "";
                        String priority = ticket.priority != null ? ticket.priority : "";

                        if (status.equalsIgnoreCase("Pending")) {
                            pending++;
                        } else if (status.equalsIgnoreCase("In Progress")) {
                            inProgress++;
                        }

                        if (priority.equalsIgnoreCase("Urgent")) {
                            urgent++;
                        }
                    }
                }
                totalTicketsText.setText(String.valueOf(total));
                tvPendingCount.setText(String.valueOf(pending));
                tvInProgressCount.setText(String.valueOf(inProgress));
                tvUrgentCount.setText(String.valueOf(urgent));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
