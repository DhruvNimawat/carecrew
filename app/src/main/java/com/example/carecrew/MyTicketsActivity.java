package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView ticketsRecyclerView;
    private TicketAdapter ticketAdapter;
    private List<Complaint> allTickets;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private MaterialButton btnAll, btnPending, btnInProgress, btnCompleted;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        allTickets = new ArrayList<>();
        ticketAdapter = new TicketAdapter(allTickets, complaint -> {
            Intent intent = new Intent(MyTicketsActivity.this, ComplaintDetailsActivity.class);
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
        ticketsRecyclerView.setAdapter(ticketAdapter);

        setupFilterButtons();
        fetchTickets();
        
        // Set initial state
        filterTickets("All");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupFilterButtons() {
        btnAll = findViewById(R.id.tabAll);
        btnPending = findViewById(R.id.tabPending);
        btnInProgress = findViewById(R.id.tabInProgress);
        btnCompleted = findViewById(R.id.tabCompleted);

        btnAll.setOnClickListener(v -> filterTickets("All"));
        btnPending.setOnClickListener(v -> filterTickets("Pending"));
        btnInProgress.setOnClickListener(v -> filterTickets("In Progress"));
        btnCompleted.setOnClickListener(v -> filterTickets("Completed"));
    }

    private void fetchTickets() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anonymous";
        
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTickets.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Complaint ticket = postSnapshot.getValue(Complaint.class);
                    if (ticket != null && currentUserId.equals(ticket.userId)) {
                        allTickets.add(ticket);
                    }
                }
                filterTickets(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyTicketsActivity.this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTickets(String status) {
        currentFilter = status;
        resetButtonStyles();
        
        List<Complaint> filteredList = new ArrayList<>();
        MaterialButton selectedButton = btnAll;

        if ("All".equals(status)) {
            filteredList = new ArrayList<>(allTickets);
            selectedButton = btnAll;
        } else {
            for (Complaint t : allTickets) {
                if (status.equalsIgnoreCase(t.status)) {
                    filteredList.add(t);
                }
            }
            if ("Pending".equals(status)) selectedButton = btnPending;
            else if ("In Progress".equals(status)) selectedButton = btnInProgress;
            else if ("Completed".equals(status)) selectedButton = btnCompleted;
        }

        // Highlight selected button
        selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        selectedButton.setTextColor(getResources().getColor(R.color.dark_blue));

        ticketAdapter.updateList(filteredList);
    }

    private void resetButtonStyles() {
        MaterialButton[] buttons = {btnAll, btnPending, btnInProgress, btnCompleted};
        for (MaterialButton b : buttons) {
            // Use setBackgroundTintList with null to clear the solid white background
            // or use a transparent color if that's preferred.
            b.setBackgroundTintList(null);
            b.setTextColor(android.graphics.Color.parseColor("#CCFFFFFF"));
        }
    }
}