package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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

public class UserDashboard extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton btnMenu;
    private TextView tvTotalTickets, tvPendingCount, tvInProgressCount, tvUrgentCount;
    private TextView tvAnnouncementTitle, tvAnnouncementMessage, tvWelcome;
    private TextView tvUserID, tvDrawerName, tvDrawerEmail;
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

        initializeViews();
        setupDrawer();
        setupClickListeners();
        
        updateStats();
        listenForAnnouncements();
        fetchUserName();
        applyEntranceAnimations();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        btnMenu = findViewById(R.id.btnMenu);

        tvTotalTickets = findViewById(R.id.userLocation); // ID from layout for total tickets
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        tvWelcome = findViewById(R.id.welcomeTitle);
        
        tvAnnouncementTitle = findViewById(R.id.tvAnnouncementTitle);
        tvAnnouncementMessage = findViewById(R.id.tvAnnouncementMessage);
    }

    private void setupClickListeners() {
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        }

        findViewById(R.id.cardRaiseComplaint).setOnClickListener(v -> 
            startActivity(new Intent(this, RaiseComplaintActivity.class)));

        findViewById(R.id.cardMyTickets).setOnClickListener(v -> 
            startActivity(new Intent(this, MyTicketsActivity.class)));

        findViewById(R.id.cardUpdates).setOnClickListener(v -> 
            startActivity(new Intent(this, RecentUpdatesActivity.class)));

        findViewById(R.id.cardProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", "User");
            startActivity(intent);
        });
    }

    private void setupDrawer() {
        TextView settingsTitle = findViewById(R.id.settingsTitle);
        if (settingsTitle != null) settingsTitle.setText(R.string.user_hub);
        
        layoutProfileDetails = findViewById(R.id.layoutProfileDetails);
        layoutAppInfoDetails = findViewById(R.id.layoutAppInfoDetails);
        layoutHelpDetails = findViewById(R.id.layoutHelpDetails);
        layoutTermsDetails = findViewById(R.id.layoutTermsDetails);

        tvUserID = findViewById(R.id.tvUserID);
        tvDrawerName = findViewById(R.id.tvDrawerAdminName);
        tvDrawerEmail = findViewById(R.id.tvDrawerAdminEmail);

        MaterialButton btnViewProfile = findViewById(R.id.btnViewProfile);
        MaterialButton btnViewAppInfo = findViewById(R.id.btnViewAppInfo);
        MaterialButton btnViewHelp = findViewById(R.id.btnViewHelp);
        MaterialButton btnViewTerms = findViewById(R.id.btnViewTerms);
        MaterialButton btnDrawerLogout = findViewById(R.id.btnDrawerLogout);
        MaterialButton btnRateUs = findViewById(R.id.btnRateUs);

        if (btnViewProfile != null) {
            btnViewProfile.setOnClickListener(v -> toggleVisibility(layoutProfileDetails));
        }
        if (btnViewAppInfo != null) {
            btnViewAppInfo.setOnClickListener(v -> toggleVisibility(layoutAppInfoDetails));
        }
        if (btnViewHelp != null) {
            btnViewHelp.setOnClickListener(v -> toggleVisibility(layoutHelpDetails));
        }
        if (btnViewTerms != null) {
            btnViewTerms.setOnClickListener(v -> toggleVisibility(layoutTermsDetails));
        }

        if (btnDrawerLogout != null) {
            btnDrawerLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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

    private void toggleVisibility(View view) {
        if (view != null) {
            view.setVisibility(view.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
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
                                    String id = snapshot.child("id").getValue(String.class);

                                    if (fullName != null && !fullName.isEmpty()) {
                                        String firstName = fullName.split(" ")[0];
                                        tvWelcome.setText(getString(R.string.welcome_user_format, firstName));
                                        tvDrawerName.setText(getString(R.string.name_label_format, fullName));
                                    }
                                    tvDrawerEmail.setText(getString(R.string.email_label_format, email));
                                    tvUserID.setText(getString(R.string.user_id_label_format, (id != null ? id : getString(R.string.not_available))));
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
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Announcement announcement = ds.getValue(Announcement.class);
                        if (announcement != null) {
                            tvAnnouncementTitle.setText(announcement.getTitle());
                            tvAnnouncementMessage.setText(announcement.getMessage());
                        }
                    }
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
                int total = 0, pending = 0, inProgress = 0, urgent = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint ticket = ds.getValue(Complaint.class);
                    if (ticket != null && userId.equals(ticket.userId)) {
                        total++;
                        String status = ticket.status != null ? ticket.status : "";
                        String priority = ticket.priority != null ? ticket.priority : "";

                        if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Open")) pending++;
                        else if (status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("Assigned")) inProgress++;

                        if (priority.equalsIgnoreCase("Urgent") || priority.equalsIgnoreCase("High")) urgent++;
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

    private void applyEntranceAnimations() {
        View headerBg = findViewById(R.id.headerBackground);
        View welcomeTitle = findViewById(R.id.welcomeTitle);
        View welcomeSubtitle = findViewById(R.id.welcomeSubtitle);
        View statsCard = findViewById(R.id.statsCard);
        View actionGrid = findViewById(R.id.actionGrid);
        View announcementCard = findViewById(R.id.announcementCard);

        headerBg.setTranslationY(-200f);
        headerBg.animate().translationY(0).setDuration(800).setInterpolator(new DecelerateInterpolator()).start();

        welcomeTitle.setAlpha(0f);
        welcomeTitle.setTranslationX(-50f);
        welcomeTitle.animate().alpha(1f).translationX(0).setDuration(600).setStartDelay(300).start();

        welcomeSubtitle.setAlpha(0f);
        welcomeSubtitle.setTranslationX(-50f);
        welcomeSubtitle.animate().alpha(1f).translationX(0).setDuration(600).setStartDelay(400).start();

        statsCard.setAlpha(0f);
        statsCard.setScaleX(0.9f);
        statsCard.setScaleY(0.9f);
        statsCard.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).setStartDelay(500).setInterpolator(new OvershootInterpolator()).start();

        if (actionGrid instanceof ViewGroup) {
            ViewGroup grid = (ViewGroup) actionGrid;
            for (int i = 0; i < grid.getChildCount(); i++) {
                View child = grid.getChildAt(i);
                child.setAlpha(0f);
                child.setScaleX(0.8f);
                child.setScaleY(0.8f);
                child.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(700 + (i * 100)).setInterpolator(new OvershootInterpolator()).start();
            }
        }

        announcementCard.setAlpha(0f);
        announcementCard.setTranslationY(100f);
        announcementCard.animate().alpha(1f).translationY(0).setDuration(700).setStartDelay(1100).start();
    }
}
