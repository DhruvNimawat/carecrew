package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private Spinner spinnerCategory;
    private MaterialButton btnAddUser;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnAddUser = findViewById(R.id.btnAddUser);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Setup Category Spinner
        String[] categories = {"Carpenter", "Electrical", "Plumber", "Housekeeping", "Tech", "Appliance Repair"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnAddUser.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String role = "Staff"; // Forced as Staff

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@bmu.edu.in")) {
            Toast.makeText(this, "Use @bmu.edu.in email only", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnAddUser.setEnabled(false);

        // Note: Creating a user via FirebaseAuth logs out the current user.
        // To avoid this, we use a secondary Firebase App instance.
        FirebaseOptions options = FirebaseApp.getInstance().getOptions();
        FirebaseApp secondaryApp;
        try {
            secondaryApp = FirebaseApp.getInstance("Secondary");
        } catch (IllegalStateException e) {
            secondaryApp = FirebaseApp.initializeApp(this, options, "Secondary");
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);

        secondaryAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = task.getResult().getUser().getUid();
                        saveUserToDatabase(name, email, role, category, uid);
                        secondaryAuth.signOut(); // Sign out from secondary app
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnAddUser.setEnabled(true);
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToDatabase(String name, String email, String role, String category, String uid) {
        String emailKey = email.replace(".", ",");
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", role);
        userMap.put("category", category);
        userMap.put("uid", uid);

        mDatabase.child("Users").child(emailKey).setValue(userMap)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnAddUser.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Staff (" + category + ") added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Database Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}