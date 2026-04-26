package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView totalTicketsText, tvPendingCount, tvInProgressCount, tvUrgentCount, welcomeTitle;
    private TextView tvAnnouncementTitle, tvAnnouncementMessage;
    private DatabaseReference mDatabase;
    private DatabaseReference mAnnouncementsRef;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");
        mAnnouncementsRef = FirebaseDatabase.getInstance().getReference().child("Announcements");
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        welcomeTitle = findViewById(R.id.welcomeTitle);
        totalTicketsText = findViewById(R.id.userLocation);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        
        tvAnnouncementTitle = findViewById(R.id.tvAnnouncementTitle);
        tvAnnouncementMessage = findViewById(R.id.tvAnnouncementMessage);

        CardView cardRaiseComplaint = findViewById(R.id.cardRaiseComplaint);
        CardView cardMyTickets = findViewById(R.id.cardMyTickets);
        CardView cardUpdates = findViewById(R.id.cardUpdates);
        CardView cardProfile = findViewById(R.id.cardProfile);

        fetchUserName();
        updateStats();
        listenForAnnouncements();

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

        android.view.View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void fetchUserName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            if (email != null) {
                String emailKey = email.replace(".", ",");
                mUsersRef.child(emailKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String fullName = snapshot.getValue(String.class);
                            if (fullName != null && !fullName.isEmpty()) {
                                String firstName = fullName.split(" ")[0];
                                welcomeTitle.setText(getString(R.string.welcome_user_format, firstName));
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
                    tvAnnouncementTitle.setText("No Announcements");
                    tvAnnouncementMessage.setText("Check back later for updates.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateStats() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        
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