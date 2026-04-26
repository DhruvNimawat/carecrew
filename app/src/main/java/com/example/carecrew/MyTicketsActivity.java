package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private View navHome, navHistory, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        String filterFromIntent = getIntent().getStringExtra("filter");
        if (filterFromIntent != null) {
            currentFilter = filterFromIntent;
        }

        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        allTickets = new ArrayList<>();
        ticketAdapter = new TicketAdapter(allTickets);
        ticketsRecyclerView.setAdapter(ticketAdapter);

        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navProfile = findViewById(R.id.navProfile);

        setupFilterButtons();
        setupBottomNav();
        fetchTickets();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDashboard.class));
            finish();
        });
        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, TicketCenterActivity.class));
            finish();
        });
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", "User");
            startActivity(intent);
            finish();
        });
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
        if (mAuth.getCurrentUser() == null) return;
        String userEmail = mAuth.getCurrentUser().getEmail();
        
        // First determine role to know how to filter
        String emailKey = userEmail != null ? userEmail.replace(".", ",") : "";
        FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot roleSnapshot) {
                String role = roleSnapshot.getValue(String.class);
                
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allTickets.clear();
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            Complaint ticket = postSnapshot.getValue(Complaint.class);
                            if (ticket != null) {
                                if (ticket.id == null) ticket.id = postSnapshot.getKey();
                                
                                if ("Staff".equalsIgnoreCase(role)) {
                                    // Staff sees tickets assigned to them
                                    if (userEmail != null && userEmail.equalsIgnoreCase(ticket.assignedTo)) {
                                        allTickets.add(ticket);
                                    }
                                } else {
                                    // Students see tickets they raised
                                    if (userEmail != null && (userEmail.equalsIgnoreCase(ticket.userId) || userEmail.equalsIgnoreCase(ticket.id))) {
                                        // Note: Some legacy tickets might use id as userId if not careful
                                        allTickets.add(ticket);
                                    }
                                }
                            }
                        }
                        filterTickets(currentFilter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning from other activities if needed
        // fetchTickets(); 
    }



    private String currentFilter = "All";


    private void filterTickets(String status) {
        currentFilter = status;
        resetButtonStyles();
        
        List<Complaint> filteredList = new ArrayList<>();
        
        // Handle Filtering Logic
        if ("All".equalsIgnoreCase(status)) {
            filteredList.addAll(allTickets);
            setActiveStyle(btnAll);
        } else if ("Assigned".equalsIgnoreCase(status)) {
            // For "My Jobs", show anything that is assigned to me and not completed
            for (Complaint t : allTickets) {
                if (!"Completed".equalsIgnoreCase(t.status) && !"Resolved".equalsIgnoreCase(t.status)) {
                    filteredList.add(t);
                }
            }
            // For UI, we might highlight 'All' or a special button if it existed
            setActiveStyle(btnAll); 
        } else {
            for (Complaint t : allTickets) {
                if (status.equalsIgnoreCase(t.status)) {
                    filteredList.add(t);
                }
            }
            // Update active button color
            if ("Pending".equalsIgnoreCase(status)) setActiveStyle(btnPending);
            else if ("In Progress".equalsIgnoreCase(status)) setActiveStyle(btnInProgress);
            else if ("Completed".equalsIgnoreCase(status) || "Resolved".equalsIgnoreCase(status)) setActiveStyle(btnCompleted);
        }
        ticketAdapter.updateList(filteredList);
    }

    private void setActiveStyle(MaterialButton btn) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        btn.setTextColor(getResources().getColor(R.color.dark_blue));
    }


    private void resetButtonStyles() {
        MaterialButton[] buttons = {btnAll, btnPending, btnInProgress, btnCompleted};
        for (MaterialButton b : buttons) {
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
            b.setTextColor(android.graphics.Color.parseColor("#CCFFFFFF"));
        }
    }
}