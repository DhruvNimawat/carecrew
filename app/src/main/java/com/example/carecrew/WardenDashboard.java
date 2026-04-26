package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WardenDashboard extends AppCompatActivity {

    private TextView tvWardenGreeting, tvActiveComplaints, tvResolvedComplaints;
    private ImageButton btnLogout;
    private View actionVerifyIssues, actionRoomInspection;
    private View navHome, navHistory, navProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_warden_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        tvWardenGreeting = findViewById(R.id.tvWardenGreeting);
        tvActiveComplaints = findViewById(R.id.tvActiveComplaints);
        tvResolvedComplaints = findViewById(R.id.tvResolvedComplaints);
        btnLogout = findViewById(R.id.btnLogout);
        actionVerifyIssues = findViewById(R.id.actionVerifyIssues);
        actionRoomInspection = findViewById(R.id.actionRoomInspection);
        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navProfile = findViewById(R.id.navProfile);

        setupClickListeners();
        fetchWardenName();
        setupStatsListener();
    }

    private void fetchWardenName() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "unknown";
            mDatabase.child("Users").child(emailKey).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        tvWardenGreeting.setText(getString(R.string.welcome_name_format, name));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupStatsListener() {
        mDatabase.child("complaints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int activeCount = 0;
                int resolvedCount = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String status = ds.child("status").getValue(String.class);
                    if (status != null) {
                        if (status.equalsIgnoreCase("Open") || status.equalsIgnoreCase("Assigned") || 
                            status.equalsIgnoreCase("In Progress") || status.equalsIgnoreCase("Started")) {
                            activeCount++;
                        } else if (status.equalsIgnoreCase("Completed") || status.equalsIgnoreCase("Resolved")) {
                            resolvedCount++;
                        }
                    }
                }
                tvActiveComplaints.setText(String.valueOf(activeCount));
                tvResolvedComplaints.setText(String.valueOf(resolvedCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupClickListeners() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(WardenDashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        actionVerifyIssues.setOnClickListener(v -> {
            // Redirect to TicketCenterActivity with role info or special filter
            Intent intent = new Intent(WardenDashboard.this, TicketCenterActivity.class);
            intent.putExtra("role", "Warden");
            startActivity(intent);
        });

        actionRoomInspection.setOnClickListener(v -> {
            Toast.makeText(this, getString(R.string.toast_room_inspection), Toast.LENGTH_SHORT).show();
        });

        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(WardenDashboard.this, TicketCenterActivity.class);
            intent.putExtra("role", "Warden");
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(WardenDashboard.this, ProfileActivity.class);
            intent.putExtra("role", "Warden");
            startActivity(intent);
        });
    }
}
