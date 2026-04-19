package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
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

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileRole, tvDetailEmail;
    private android.view.View navHome, navRaise, navTickets, navProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvDetailEmail = findViewById(R.id.tvDetailEmail);

        // Bottom Nav
        navHome = findViewById(R.id.navHome);
        navRaise = findViewById(R.id.navRaise);
        navTickets = findViewById(R.id.navTickets);
        navProfile = findViewById(R.id.navProfile);

        setupBottomNav();
        fetchUserData();

        findViewById(R.id.btnProfileLogout).setOnClickListener(v -> logout());
        
        findViewById(R.id.btnAbout).setOnClickListener(v -> {
            Toast.makeText(this, "CareCrew v1.0 - Campus Maintenance Management", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchUserData() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            tvDetailEmail.setText(email);
            
            String emailKey = email != null ? email.replace(".", ",") : "unknown";
            mDatabase.child("Users").child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String role = snapshot.child("role").getValue(String.class);
                        tvProfileName.setText(name);
                        tvProfileRole.setText(role);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            // Determine which dashboard to go back to
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "";
            mDatabase.child("Users").child(emailKey).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    Intent intent = null;
                    if ("Admin".equalsIgnoreCase(role)) intent = new Intent(ProfileActivity.this, AdminDashboard.class);
                    else {
                        Toast.makeText(ProfileActivity.this, role + " Dashboard coming soon!", Toast.LENGTH_SHORT).show();
                    }
                    
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });

        // Other tabs can be implemented similarly
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}