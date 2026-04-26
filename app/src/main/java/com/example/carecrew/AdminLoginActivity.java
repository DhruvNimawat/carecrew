package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashMap;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private MaterialButton loginButton;
    private LinearLayout btnBackToRoles;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

                if (!email.endsWith("@bmu.edu.in")) {
                    Toast.makeText(this, "Use college email only", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    passwordEditText.setError("Password is required");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                checkUserRoleAndNavigate(email);
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }

    private void checkUserRoleAndNavigate(String email) {
        String emailKey = email.replace(".", ",");
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(emailKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.child("role").getValue(String.class);
                            if ("Admin".equalsIgnoreCase(role)) {
                                Toast.makeText(AdminLoginActivity.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AdminLoginActivity.this, AdminDashboard.class));
                                finish();
                            } else {
                                mAuth.signOut();
                                Toast.makeText(AdminLoginActivity.this, "Access Denied: This account is registered as " + role, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mAuth.signOut();
                            Toast.makeText(AdminLoginActivity.this, "Error: User record not found.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        mAuth.signOut();
                        Toast.makeText(AdminLoginActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}