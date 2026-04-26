package com.example.carecrew;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComplaintReviewActivity extends AppCompatActivity {

    private String category, priority, description, block, floor, roomNumber, imageUrl;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_review);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve data from Intent
        category = getIntent().getStringExtra("category");
        priority = getIntent().getStringExtra("priority");
        description = getIntent().getStringExtra("description");
        block = getIntent().getStringExtra("block");
        floor = getIntent().getStringExtra("floor");
        roomNumber = getIntent().getStringExtra("roomNumber");
        imageUrl = getIntent().getStringExtra("imageUrl");

        // Set Review Text
        ((TextView) findViewById(R.id.reviewCategory)).setText(category);
        ((TextView) findViewById(R.id.reviewDescription)).setText(description);
        ((TextView) findViewById(R.id.reviewLocation)).setText(block + ", " + floor + ", " + roomNumber);
        ((TextView) findViewById(R.id.reviewPriority)).setText(priority);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEdit).setOnClickListener(v -> finish());

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            submitComplaint();
        });
    }

    private void submitComplaint() {
        if (mAuth.getCurrentUser() == null) return;
        String email = mAuth.getCurrentUser().getEmail();
        String emailKey = email != null ? email.replace(".", ",") : "unknown";

        mDatabase.child("Users").child(emailKey).child("name").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                String userName = snapshot.exists() ? snapshot.getValue(String.class) : "Unknown User";
                finalizeSubmission(userName);
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                finalizeSubmission("Unknown User");
            }
        });
    }

    private void finalizeSubmission(String userName) {
        String complaintId = mDatabase.child("complaints").push().getKey();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        String timestamp = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());

        Complaint complaint = new Complaint();
        complaint.id = complaintId;
        complaint.userId = userId;
        complaint.userName = userName;
        complaint.category = category;
        complaint.description = description;
        complaint.block = block;
        complaint.floor = floor;
        complaint.roomNumber = roomNumber;
        complaint.priority = priority;
        complaint.status = "Open";
        complaint.imageUrl = imageUrl;
        complaint.timestamp = timestamp;

        if (complaintId != null) {
            mDatabase.child("complaints").child(complaintId).setValue(complaint)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Ticket Submitted Successfully!", Toast.LENGTH_LONG).show();
                            finishAffinity();
                            startActivity(new android.content.Intent(this, UserDashboardActivity.class));
                        } else {
                            Toast.makeText(this, "Submission failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}