package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WardenLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private LinearLayout btnBackToRoles;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warden_login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        btnBackToRoles = findViewById(R.id.btnBackToRoles);

        // Back to Roles Click
        if (btnBackToRoles != null) {
            btnBackToRoles.setOnClickListener(v -> finish());
        }

        // Login Button Click
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    emailEditText.setError("Email is required");
                    return;
                }
                if (password.isEmpty()) {
                    passwordEditText.setError("Password is required");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            checkUserRoleAndNavigate(email);
                        } else {
                            android.util.Log.e("WardenLogin", "Auth Failed", task.getException());
                            Toast.makeText(WardenLoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            });
        }
    }

    private void checkUserRoleAndNavigate(String email) {
        String emailKey = email.replace(".", ",");
        android.util.Log.d("WardenLogin", "Checking role for: " + emailKey);
        FirebaseDatabase.getInstance().getReference()
            .child("Users").child(emailKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    android.util.Log.d("WardenLogin", "DataSnapshot: " + snapshot.toString());
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        android.util.Log.d("WardenLogin", "Found role: " + role);
                        if ("Warden".equalsIgnoreCase(role)) {
                            Toast.makeText(WardenLoginActivity.this, "Warden Login Successful!", Toast.LENGTH_SHORT).show();
                            android.util.Log.d("WardenLogin", "Attempting to start WardenDashboard Activity");
                            Intent intent = new Intent(WardenLoginActivity.this, WardenDashboard.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            mAuth.signOut();
                            String actualRole = (role != null) ? role : "No Role";
                            Toast.makeText(WardenLoginActivity.this, "Access Denied: Logged in as " + actualRole + ". This page is for Wardens only.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mAuth.signOut();
                        android.util.Log.e("WardenLogin", "User not found in DB");
                        Toast.makeText(WardenLoginActivity.this, "Error: User record not found in database.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    mAuth.signOut();
                    android.util.Log.e("WardenLogin", "DB Error", error.toException());
                    Toast.makeText(WardenLoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
