package com.example.carecrew;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TicketCenterActivity extends AppCompatActivity {

    private TextView tvCountUnassigned, tvCountAssigned, tvCountCompleted, tvCountDelayed;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_center);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Tickets");

        // Initialize Views
        tvCountUnassigned = findViewById(R.id.tvCountUnassigned);
        tvCountAssigned = findViewById(R.id.tvCountAssigned);
        tvCountCompleted = findViewById(R.id.tvCountCompleted);
        tvCountDelayed = findViewById(R.id.tvCountDelayed);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupCardListeners();
        setupRealtimeCounters();
    }

    private void setupRealtimeCounters() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unassigned = 0;
                int assigned = 0;
                int completed = 0;
                int delayed = 0;

                for (DataSnapshot ticket : snapshot.getChildren()) {
                    String status = ticket.child("status").getValue(String.class);
                    if (status == null) continue;

                    switch (status.toLowerCase()) {
                        case "open":
                        case "unassigned":
                            unassigned++;
                            break;
                        case "assigned":
                        case "in progress":
                        case "pending":
                            assigned++;
                            break;
                        case "completed":
                        case "resolved":
                        case "closed":
                            completed++;
                            break;
                        case "delayed":
                        case "overdue":
                            delayed++;
                            break;
                    }
                }

                tvCountUnassigned.setText(String.format("%02d", unassigned));
                tvCountAssigned.setText(String.format("%02d", assigned));
                tvCountCompleted.setText(String.format("%02d", completed));
                tvCountDelayed.setText(delayed + " Delayed");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketCenterActivity.this, "Error loading statistics", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCardListeners() {
        findViewById(R.id.cardUnassigned).setOnClickListener(v -> showToast("Opening Unassigned Tickets"));
        findViewById(R.id.cardAssigned).setOnClickListener(v -> showToast("Opening In-Progress Tickets"));
        findViewById(R.id.cardCompleted).setOnClickListener(v -> showToast("Opening Completed Tickets"));
        findViewById(R.id.cardDelayed).setOnClickListener(v -> showToast("Opening Delayed Tickets"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}