package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TicketDetailsStaffActivity extends AppCompatActivity {

    private TextView tvTicketId, tvCategory, tvLocation, tvPriority, tvDescription;
    private ImageView ivComplaintImage;
    private View imagePlaceholder, cardDescription;
    private MaterialButton btnAcceptJob, btnReject;
    
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String ticketId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details_staff);

        // Entrance animation
        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(500).start();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        initViews();
        loadTicketData();

        btnAcceptJob.setOnClickListener(v -> acceptJob());
        btnReject.setOnClickListener(v -> finish());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tvTicketId = findViewById(R.id.tvTicketId);
        tvCategory = findViewById(R.id.tvCategory);
        tvLocation = findViewById(R.id.tvLocation);
        tvPriority = findViewById(R.id.tvPriority);
        tvDescription = findViewById(R.id.tvDescription);
        ivComplaintImage = findViewById(R.id.ivComplaintImage);
        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        cardDescription = findViewById(R.id.cardDescription);
        btnAcceptJob = findViewById(R.id.btnAcceptJob);
        btnReject = findViewById(R.id.btnReject);

        cardDescription.setOnClickListener(v -> {
            if (tvDescription.getMaxLines() == 5) {
                tvDescription.setMaxLines(Integer.MAX_VALUE);
                tvDescription.setEllipsize(null);
            } else {
                tvDescription.setMaxLines(5);
                tvDescription.setEllipsize(android.text.TextUtils.TruncateAt.END);
            }
        });
    }

    private void loadTicketData() {
        ticketId = getIntent().getStringExtra("ticketId");
        String category = getIntent().getStringExtra("category");
        String location = getIntent().getStringExtra("location");
        String priority = getIntent().getStringExtra("priority");
        String description = getIntent().getStringExtra("description");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String status = getIntent().getStringExtra("status");

        tvTicketId.setText(ticketId != null ? getString(R.string.ticket_id_prefix, ticketId.substring(Math.max(0, ticketId.length() - 6)).toUpperCase()) : getString(R.string.not_available));
        tvCategory.setText(category);
        tvLocation.setText(location);
        tvPriority.setText(priority);
        tvDescription.setText(description);

        if ("Cancelled".equalsIgnoreCase(status)) {
            findViewById(R.id.headerView).setBackgroundResource(R.drawable.gradient_header_red);
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imagePlaceholder.setVisibility(View.GONE);
            // In a real app, use Glide or Picasso to load imageUrl into ivComplaintImage
            // Glide.with(this).load(imageUrl).into(ivComplaintImage);
        } else {
            imagePlaceholder.setVisibility(View.VISIBLE);
        }
        
        // Adjust priority color
        if ("Urgent".equalsIgnoreCase(priority) || "High".equalsIgnoreCase(priority)) {
            tvPriority.setBackgroundResource(R.drawable.bg_status_urgent);
            tvPriority.setTextColor(getResources().getColor(android.R.color.white));
        }
    }

    private void acceptJob() {
        if (mAuth.getCurrentUser() == null || ticketId == null) return;
        String staffEmail = mAuth.getCurrentUser().getEmail();

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Assigned");
        updates.put("assignedTo", staffEmail);

        mDatabase.child(ticketId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.toast_ticket_accepted), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.toast_failed_accept), Toast.LENGTH_SHORT).show());
    }

    private void rejectJob() {
        if (mAuth.getCurrentUser() == null || ticketId == null) return;
        String userEmail = mAuth.getCurrentUser().getEmail();
        String emailKey = userEmail != null ? userEmail.replace(".", ",") : "unknown";

        // Mark as rejected by this staff so it doesn't show in their Available list
        mDatabase.child(ticketId).child("rejectedBy").child(emailKey).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, getString(R.string.toast_job_rejected), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}