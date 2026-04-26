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

    private TextView totalTicketsText, tvPendingCount, tvInProgressCount, tvUrgentCount;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        totalTicketsText = findViewById(R.id.userLocation);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvInProgressCount = findViewById(R.id.tvInProgressCount);
        tvUrgentCount = findViewById(R.id.tvUrgentCount);

        CardView cardRaiseComplaint = findViewById(R.id.cardRaiseComplaint);
        CardView cardMyTickets = findViewById(R.id.cardMyTickets);
        CardView cardUpdates = findViewById(R.id.cardUpdates);
        CardView cardProfile = findViewById(R.id.cardProfile);

        updateStats();

        cardRaiseComplaint.setOnClickListener(v -> 
            startActivity(new Intent(UserDashboardActivity.this, RaiseTicketActivity.class))
        );

        cardMyTickets.setOnClickListener(v -> 
            startActivity(new Intent(UserDashboardActivity.this, TicketCenterActivity.class))
        );

        cardUpdates.setOnClickListener(v -> 
            android.widget.Toast.makeText(this, getString(R.string.toast_recent_updates), android.widget.Toast.LENGTH_SHORT).show()
        );

        cardProfile.setOnClickListener(v -> 
            startActivity(new Intent(UserDashboardActivity.this, ProfileDetailsActivity.class))
        );

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navHome).setOnClickListener(v -> {
            // Already home
        });

        findViewById(R.id.navHistory).setOnClickListener(v -> 
            startActivity(new Intent(UserDashboardActivity.this, TicketCenterActivity.class))
        );

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, ProfileActivity.class);
            intent.putExtra("role", "User");
            startActivity(intent);
        });

        // Add listeners for Profile and Updates as needed
    }

    private void updateStats() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
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