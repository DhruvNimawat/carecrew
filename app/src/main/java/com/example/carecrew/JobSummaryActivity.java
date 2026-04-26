package com.example.carecrew;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JobSummaryActivity extends AppCompatActivity {

    private String ticketId;
    private TextView tvTicketId, tvTimeTaken, tvRoom, tvCategory, tvHandledBy, tvClosedAt, tvNotes;
    private ImageView ivBefore, ivAfter;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_summary);

        ticketId = getIntent().getStringExtra("ticketId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints").child(ticketId);

        tvTicketId = findViewById(R.id.tvSummaryTicketId);
        tvTimeTaken = findViewById(R.id.tvTimeTaken);
        tvRoom = findViewById(R.id.tvSummaryRoom);
        tvCategory = findViewById(R.id.tvSummaryCategory);
        tvHandledBy = findViewById(R.id.tvSummaryHandledBy);
        tvClosedAt = findViewById(R.id.tvSummaryClosedAt);
        tvNotes = findViewById(R.id.tvSummaryNotes);
        ivBefore = findViewById(R.id.ivSummaryBefore);
        ivAfter = findViewById(R.id.ivSummaryAfter);

        loadSummaryData();

        findViewById(R.id.btnDone).setOnClickListener(v -> {
            finishAffinity();
            startActivity(new android.content.Intent(this, StaffDashboard.class));
        });
    }

    private void loadSummaryData() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint ticket = snapshot.getValue(Complaint.class);
                if (ticket != null) {
                    tvTicketId.setText("Ticket ID: " + (ticketId.length() > 6 ? ticketId.substring(ticketId.length()-6).toUpperCase() : ticketId));
                    tvRoom.setText(ticket.roomNumber);
                    tvCategory.setText(ticket.category);
                    tvHandledBy.setText(ticket.staffName != null ? ticket.staffName : "Staff Member");
                    tvClosedAt.setText(ticket.closedAt != null ? ticket.closedAt : "--:--");
                    tvNotes.setText(ticket.repairNotes != null ? ticket.repairNotes : "No notes provided.");

                    long completionTime = 0;
                    if (ticket.completionTimeMillis instanceof Long) {
                        completionTime = (Long) ticket.completionTimeMillis;
                    } else if (ticket.completionTimeMillis instanceof String) {
                        try {
                            completionTime = Long.parseLong((String) ticket.completionTimeMillis);
                        } catch (Exception ignored) {}
                    }

                    if (completionTime > 0 && ticket.startTimeMillis > 0) {
                        long diff = completionTime - ticket.startTimeMillis;
                        long minutes = (diff / (1000 * 60));
                        tvTimeTaken.setText("Time taken: " + minutes + " minutes");
                    } else {
                        tvTimeTaken.setText("Time taken: N/A");
                    }

                    if (ticket.startWorkImageUrl != null) Glide.with(JobSummaryActivity.this).load(ticket.startWorkImageUrl).into(ivBefore);
                    if (ticket.afterRepairImageUrl != null) Glide.with(JobSummaryActivity.this).load(ticket.afterRepairImageUrl).into(ivAfter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}