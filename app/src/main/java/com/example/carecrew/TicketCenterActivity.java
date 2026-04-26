package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.List;

public class TicketCenterActivity extends AppCompatActivity {

    private TextView tvCountUnassigned, tvCountAssigned, tvCountCompleted, tvCountDelayed;
    private DatabaseReference mDatabase;

    private RecyclerView rvRecentTickets;
    private TicketAdapter ticketAdapter;
    private List<Complaint> ticketList;
    private List<Complaint> fullTicketList;
    private AutoCompleteTextView etSearch;
    private LinearLayout layoutOtherOptions;
    private TextView tvRecentTicketsHeader, tvSubtitle;

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
        rvRecentTickets = findViewById(R.id.rvRecentTickets);
        etSearch = findViewById(R.id.etSearch);
        layoutOtherOptions = findViewById(R.id.layoutOtherOptions);
        tvRecentTicketsHeader = findViewById(R.id.tvRecentTicketsHeader);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        // Setup RecyclerView
        ticketList = new ArrayList<>();
        fullTicketList = new ArrayList<>();
        ticketAdapter = new TicketAdapter(ticketList, complaint -> {
            Intent intent = new Intent(TicketCenterActivity.this, ComplaintDetailsActivity.class);
            intent.putExtra("id", complaint.id);
            intent.putExtra("category", complaint.category);
            intent.putExtra("status", complaint.status);
            intent.putExtra("description", complaint.description);
            intent.putExtra("block", complaint.block);
            intent.putExtra("floor", complaint.floor);
            intent.putExtra("roomNumber", complaint.roomNumber);
            intent.putExtra("priority", complaint.priority);
            intent.putExtra("timestamp", complaint.getTimestampString());
            intent.putExtra("assignedTo", complaint.assignedTo);
            startActivity(intent);
        });
        rvRecentTickets.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTickets.setAdapter(ticketAdapter);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (layoutOtherOptions.getVisibility() == View.GONE) {
                // If a filter is applied (status cards or search), go back to "Recent Tickets" view
                etSearch.setText("");
                layoutOtherOptions.setVisibility(View.VISIBLE);
                tvRecentTicketsHeader.setVisibility(View.VISIBLE);
                tvRecentTicketsHeader.setText("Recent Tickets");
                if (tvSubtitle != null) tvSubtitle.setVisibility(View.VISIBLE);
                
                ticketList.clear();
                ticketList.addAll(fullTicketList);
                ticketAdapter.notifyDataSetChanged();
            } else {
                // If already on the main view, finish activity
                finish();
            }
        });

        setupCardListeners();
        setupRealtimeCountersAndList();
        setupSearch();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<Complaint> filteredList = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            layoutOtherOptions.setVisibility(View.VISIBLE);
            tvRecentTicketsHeader.setVisibility(View.VISIBLE);
            tvRecentTicketsHeader.setText("Recent Tickets");
            if (tvSubtitle != null) tvSubtitle.setVisibility(View.VISIBLE);
            filteredList.addAll(fullTicketList);
        } else {
            layoutOtherOptions.setVisibility(View.GONE);
            tvRecentTicketsHeader.setVisibility(View.GONE);
            if (tvSubtitle != null) tvSubtitle.setVisibility(View.GONE);
            
            String query = text.toLowerCase().trim();
            for (Complaint item : fullTicketList) {
                boolean matches = false;
                if (item.category != null && item.category.toLowerCase().contains(query)) {
                    matches = true;
                    if (!suggestions.contains(item.category)) suggestions.add(item.category);
                }
                if (item.description != null && item.description.toLowerCase().contains(query)) {
                    matches = true;
                }
                if (item.id != null && item.id.toLowerCase().contains(query)) {
                    matches = true;
                    if (!suggestions.contains(item.id)) suggestions.add(item.id);
                }

                if (matches) {
                    filteredList.add(item);
                }
            }
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        etSearch.setAdapter(adapter);

        ticketList.clear();
        ticketList.addAll(filteredList);
        ticketAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (etSearch.getText().length() > 0) {
            etSearch.setText("");
        } else {
            super.onBackPressed();
        }
    }

    private void setupRealtimeCountersAndList() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int unassigned = 0;
                int assigned = 0;
                int completed = 0;
                int delayed = 0;
                fullTicketList.clear();

                for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                    Complaint ticket = ticketSnapshot.getValue(Complaint.class);
                    if (ticket == null) continue;

                    if (ticket.id == null || ticket.id.isEmpty()) {
                        ticket.id = ticketSnapshot.getKey();
                    }

                    // Add to list
                    fullTicketList.add(ticket);

                    String status = ticket.status;
                    if (status == null) continue;

                    switch (status.toLowerCase()) {
                        case "open":
                        case "unassigned":
                        case "pending":
                            unassigned++;
                            break;
                        case "assigned":
                        case "in progress":
                            assigned++;
                            break;
                        case "completed":
                        case "resolved":
                        case "closed":
                            completed++;
                            break;
                        case "delayed":
                        case "overdue":
                            // We don't increment delayed here, we'll calculate it below
                            break;
                    }
                }

                // Calculate delayed tickets (unassigned for > 48 hours)
                long currentTime = System.currentTimeMillis();
                long fortyEightHours = 48 * 60 * 60 * 1000L;
                for (Complaint ticket : fullTicketList) {
                    long ticketTimestamp = ticket.getTimestampLong();
                    if (ticketTimestamp == 0) continue;
                    long age = currentTime - ticketTimestamp;
                    boolean isUnassigned = (ticket.assignedTo == null || ticket.assignedTo.isEmpty());
                    boolean isNotCompleted = (ticket.status == null || (!ticket.status.equalsIgnoreCase("Completed") && !ticket.status.equalsIgnoreCase("Resolved") && !ticket.status.equalsIgnoreCase("Closed")));
                    if (age >= fortyEightHours && isUnassigned && isNotCompleted) {
                        delayed++;
                    }
                }

                // Sort list by timestamp (descending - latest first)
                Collections.sort(fullTicketList, (c1, c2) -> Long.compare(c2.getTimestampLong(), c1.getTimestampLong()));

                // Update UI
                tvCountUnassigned.setText(String.format("%02d", unassigned));
                tvCountAssigned.setText(String.format("%02d", assigned));
                tvCountCompleted.setText(String.format("%02d", completed));
                tvCountDelayed.setText(delayed + " Delayed");

                filter(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketCenterActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCardListeners() {
        View cardUnassigned = findViewById(R.id.cardUnassigned);
        View cardAssigned = findViewById(R.id.cardAssigned);
        View cardCompleted = findViewById(R.id.cardCompleted);

        if (cardUnassigned != null) cardUnassigned.setOnClickListener(v -> filterByStatus("unassigned"));
        if (cardAssigned != null) cardAssigned.setOnClickListener(v -> filterByStatus("assigned"));
        if (cardCompleted != null) cardCompleted.setOnClickListener(v -> filterByStatus("completed"));

        View btnStaffAllocation = findViewById(R.id.btnStaffAllocation);
        if (btnStaffAllocation != null) {
            btnStaffAllocation.setOnClickListener(v -> {
                filterByStatus("unassigned");
                tvRecentTicketsHeader.setText("Staff Allocation (Unassigned)");
            });
        }

        CardView cardDelayed = findViewById(R.id.cardDelayed);
        if (cardDelayed != null) {
            cardDelayed.setOnClickListener(v -> {
                showDelayedTickets();
            });
        }
    }

    private void filterByStatus(String statusQuery) {
        etSearch.setText(""); // Clear search
        layoutOtherOptions.setVisibility(View.GONE);
        tvRecentTicketsHeader.setVisibility(View.VISIBLE);
        tvRecentTicketsHeader.setText(statusQuery.substring(0, 1).toUpperCase() + statusQuery.substring(1) + " Tickets");
        if (tvSubtitle != null) tvSubtitle.setVisibility(View.GONE);

        List<Complaint> filteredList = new ArrayList<>();
        for (Complaint item : fullTicketList) {
            String status = item.status != null ? item.status.toLowerCase() : "";
            if (statusQuery.equals("unassigned")) {
                if (status.equals("open") || status.equals("unassigned") || status.equals("pending")) {
                    filteredList.add(item);
                }
            } else if (statusQuery.equals("assigned")) {
                if (status.equals("assigned") || status.equals("in progress")) {
                    filteredList.add(item);
                }
            } else if (statusQuery.equals("completed")) {
                if (status.equals("completed") || status.equals("resolved") || status.equals("closed")) {
                    filteredList.add(item);
                }
            }
        }

        ticketList.clear();
        ticketList.addAll(filteredList);
        ticketAdapter.notifyDataSetChanged();
    }

    private void showDelayedTickets() {
        List<Complaint> delayedTickets = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        long fortyEightHours = 48 * 60 * 60 * 1000L;

        for (Complaint ticket : fullTicketList) {
            long ticketTimestamp = ticket.getTimestampLong();
            if (ticketTimestamp == 0) continue;
            
            long age = currentTime - ticketTimestamp;
            // Overdue if not assigned for more than 48 hours
            boolean isOverdue = (age >= fortyEightHours);
            boolean isUnassigned = (ticket.assignedTo == null || ticket.assignedTo.isEmpty());
            boolean isNotCompleted = (ticket.status == null || (!ticket.status.equalsIgnoreCase("Completed") && !ticket.status.equalsIgnoreCase("Resolved") && !ticket.status.equalsIgnoreCase("Closed")));

            if (isOverdue && isUnassigned && isNotCompleted) {
                delayedTickets.add(ticket);
            }
        }

        if (delayedTickets.isEmpty()) {
            Toast.makeText(this, "No tickets overdue (> 48h unassigned)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display them
        layoutOtherOptions.setVisibility(View.GONE);
        tvRecentTicketsHeader.setVisibility(View.VISIBLE);
        tvRecentTicketsHeader.setText("Maintenance Overdue (>48h)");
        if (tvSubtitle != null) tvSubtitle.setVisibility(View.GONE);
        
        ticketList.clear();
        ticketList.addAll(delayedTickets);
        ticketAdapter.notifyDataSetChanged();
        
        etSearch.setText(""); // Clear search to avoid confusion
        Toast.makeText(this, "Showing " + delayedTickets.size() + " overdue tickets", Toast.LENGTH_SHORT).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}