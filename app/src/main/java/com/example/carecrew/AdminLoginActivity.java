package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
    private TextView loginTitle, loginSubTitle;
    private View loginHeaderBg, loginHeaderCurve;
    private androidx.cardview.widget.CardView mainLogoCard;

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
        loginTitle = findViewById(R.id.loginTitle);
        loginSubTitle = findViewById(R.id.loginSubTitle);
        loginHeaderBg = findViewById(R.id.loginHeaderBg);
        loginHeaderCurve = findViewById(R.id.loginHeaderCurve);
        mainLogoCard = findViewById(R.id.mainLogoCard);

        // Apply animations
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation topDown = AnimationUtils.loadAnimation(this, R.anim.top_down);

        if (loginHeaderBg != null) loginHeaderBg.startAnimation(topDown);
        if (loginHeaderCurve != null) loginHeaderCurve.startAnimation(topDown);
        if (mainLogoCard != null) mainLogoCard.startAnimation(fadeIn);
        if (loginTitle != null) loginTitle.startAnimation(fadeIn);
        if (loginSubTitle != null) loginSubTitle.startAnimation(fadeIn);
        if (findViewById(R.id.loginCard) != null) {
            Animation popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in);
            findViewById(R.id.loginCard).startAnimation(popIn);
        }

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
                                Toast.makeText(this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AdminLoginActivity.this, AdminDashboard.class));
                                finish();
                            } else {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }
}