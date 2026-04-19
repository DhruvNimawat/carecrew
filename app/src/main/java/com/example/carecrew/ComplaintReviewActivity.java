package com.example.carecrew;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
        ((TextView) findViewById(R.id.reviewCategory)).setText(category != null ? category : "N/A");
        ((TextView) findViewById(R.id.reviewDescription)).setText(description != null ? description : "No description provided");
        
        String location = "";
        if (block != null && !block.isEmpty()) location += block;
        if (floor != null && !floor.isEmpty()) location += (location.isEmpty() ? "" : ", ") + floor;
        if (roomNumber != null && !roomNumber.isEmpty()) location += (location.isEmpty() ? "" : ", ") + "Room " + roomNumber;
        ((TextView) findViewById(R.id.reviewLocation)).setText(location.isEmpty() ? "No location specified" : location);
        
        ((TextView) findViewById(R.id.reviewPriority)).setText(priority != null ? priority : "Normal");

        // Handle Image Preview
        android.widget.ImageView ivReviewImage = findViewById(R.id.ivReviewImage);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // If you have Glide or Picasso, use them here. 
            // For now, we just ensure the view is visible or has a placeholder.
            ivReviewImage.setAlpha(1.0f);
            ivReviewImage.setPadding(0, 0, 0, 0);
            ivReviewImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            // Note: In a real app, use Glide.with(this).load(imageUrl).into(ivReviewImage);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEdit).setOnClickListener(v -> finish());

        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            submitComplaint();
        });
    }

    private void submitComplaint() {
        String complaintId = mDatabase.child("complaints").push().getKey();
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        long timestamp = System.currentTimeMillis();

        Complaint complaint = new Complaint(
                complaintId,
                userId,
                category,
                description,
                block,
                floor,
                roomNumber,
                priority,
                "Pending",
                imageUrl,
                String.valueOf(timestamp)
        );

        if (complaintId != null) {
            mDatabase.child("complaints").child(complaintId).setValue(complaint)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Ticket Submitted Successfully!", Toast.LENGTH_LONG).show();
                            finishAffinity();
                            startActivity(new android.content.Intent(this, UserDashboardActivity.class));
                        } else {
                            Toast.makeText(this, "Submission failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}