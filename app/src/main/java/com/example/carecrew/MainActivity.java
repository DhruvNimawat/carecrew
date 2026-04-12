package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Show Splash Screen first
        showSplashScreen();
    }

    private void showSplashScreen() {
        setContentView(R.layout.activity_splash);
        
        // Apply window insets to the root view
        applyWindowInsets(findViewById(android.R.id.content));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showRoleSelection();
            }
        }, SPLASH_TIME);
    }

    private void showRoleSelection() {
        setContentView(R.layout.activity_role_selection);
        
        // Apply window insets to the new root view
        applyWindowInsets(findViewById(android.R.id.content));

        // Initialize Role Selection Cards
        CardView cardStudent = findViewById(R.id.cardStudent);
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        CardView cardStaff = findViewById(R.id.cardStaff);
        CardView cardWarden = findViewById(R.id.cardWarden);

        if (cardStudent != null) {
            cardStudent.setOnClickListener(v -> onRoleSelected("Student"));
        }
        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> onRoleSelected("Admin"));
        }
        if (cardStaff != null) {
            cardStaff.setOnClickListener(v -> onRoleSelected("Staff"));
        }
        if (cardWarden != null) {
            cardWarden.setOnClickListener(v -> onRoleSelected("Warden"));
        }
    }

    private void applyWindowInsets(View view) {
        if (view != null) {
            ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void onRoleSelected(String role) {
        Toast.makeText(this, "Welcome " + role, Toast.LENGTH_SHORT).show();
        // Navigate to UserLoginActivity
        Intent intent = new Intent(MainActivity.this, UserLoginActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}