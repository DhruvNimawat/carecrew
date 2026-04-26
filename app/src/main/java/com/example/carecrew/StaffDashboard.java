package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StaffDashboard extends AppCompatActivity {

    private TextView tvAssignedTasks, tvPendingTasks, tvCompletedTasks, tvWelcomeTitle;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvAssignedTasks = findViewById(R.id.tvAssignedTasks);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvWelcomeTitle = findViewById(R.id.welcomeTitle);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(StaffDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Action Grid Listeners
        findViewById(R.id.cardMyTasks).setOnClickListener(v -> 
            startActivity(new Intent(StaffDashboard.this, TicketCenterActivity.class))
        );

        findViewById(R.id.cardStaffProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", "Staff");
            startActivity(intent);
        });

        fetchStaffName();
        fetchStaffStats();
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
                        tvWelcomeTitle.setText("Welcome, " + name);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void fetchStaffStats() {
        // Logic to fetch tasks assigned to this staff member
        // For now, we show total stats as a placeholder
        mDatabase.child("complaints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int assigned = 0;
                int pending = 0;
                int completed = 0;

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Complaint ticket = postSnapshot.getValue(Complaint.class);
                    if (ticket != null) {
                        assigned++;
                        if ("Pending".equalsIgnoreCase(ticket.status) || "Open".equalsIgnoreCase(ticket.status)) {
                            pending++;
                        } else if ("Completed".equalsIgnoreCase(ticket.status) || "Resolved".equalsIgnoreCase(ticket.status)) {
                            completed++;
                        }
                    }
                }
                tvAssignedTasks.setText(String.valueOf(assigned));
                tvPendingTasks.setText(String.valueOf(pending));
                tvCompletedTasks.setText(String.valueOf(completed));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}