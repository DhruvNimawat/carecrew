package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TicketCenterActivity extends AppCompatActivity {

    private TextView tvCountUnassigned, tvCountAssigned, tvCountCompleted, tvCountDelayed;
    private DatabaseReference mDatabase;
    private RecyclerView rvTickets;
    private TicketAdapter ticketAdapter;
    private List<Complaint> allTicketsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_center);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        // Initialize Views
        tvCountUnassigned = findViewById(R.id.tvCountUnassigned);
        tvCountAssigned = findViewById(R.id.tvCountAssigned);
        tvCountCompleted = findViewById(R.id.tvCountCompleted);
        tvCountDelayed = findViewById(R.id.tvCountDelayed);
        rvTickets = findViewById(R.id.rvTickets);

        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        ticketAdapter = new TicketAdapter(allTicketsList, complaint -> {
            Intent intent = new Intent(TicketCenterActivity.this, ComplaintDetailsActivity.class);
            intent.putExtra("id", complaint.id);
            intent.putExtra("category", complaint.category);
            intent.putExtra("status", complaint.status);
            intent.putExtra("description", complaint.description);
            intent.putExtra("block", complaint.block);
            intent.putExtra("floor", complaint.floor);
            intent.putExtra("roomNumber", complaint.roomNumber);
            intent.putExtra("priority", complaint.priority);
            intent.putExtra("timestamp", complaint.timestamp);
            intent.putExtra("assignedTo", complaint.assignedTo);
            intent.putExtra("rating", complaint.rating);
            intent.putExtra("review", complaint.review);
            startActivity(intent);
        });
        rvTickets.setAdapter(ticketAdapter);

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
                allTicketsList.clear();

                for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                    Complaint ticket = ticketSnapshot.getValue(Complaint.class);
                    if (ticket == null) continue;

                    allTicketsList.add(ticket);
                    String status = ticket.status;
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

                ticketAdapter.notifyDataSetChanged();
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