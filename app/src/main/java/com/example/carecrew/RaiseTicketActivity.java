package com.example.carecrew;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RaiseTicketActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private TextInputEditText etTitle, etDescription;
    private RadioGroup rgPriority;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_raise_ticket);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        spinnerCategory = findViewById(R.id.spinnerCategory);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        rgPriority = findViewById(R.id.rgPriority);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnSubmit).setOnClickListener(v -> submitTicket());
    }

    private void submitTicket() {
        String category = spinnerCategory.getSelectedItem().toString();
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        int selectedPriorityId = rgPriority.getCheckedRadioButtonId();
        RadioButton rbPriority = findViewById(selectedPriorityId);
        String priority = rbPriority != null ? rbPriority.getText().toString() : "Medium";

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) return;
        String userEmail = mAuth.getCurrentUser().getEmail();
        String ticketId = mDatabase.child("complaints").push().getKey();

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("ticketId", ticketId);
        ticket.put("userEmail", userEmail);
        ticket.put("category", category);
        ticket.put("title", title);
        ticket.put("description", description);
        ticket.put("priority", priority);
        ticket.put("status", "Open");
        ticket.put("timestamp", System.currentTimeMillis());

        if (ticketId != null) {
            mDatabase.child("complaints").child(ticketId).setValue(ticket)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RaiseTicketActivity.this, "Ticket raised successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RaiseTicketActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
