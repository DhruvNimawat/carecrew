package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class AllTicketsActivity extends AppCompatActivity {

    private RecyclerView rvAllTickets;
    private TicketAdapter ticketAdapter;
    private List<Complaint> ticketList;
    private List<Complaint> fullTicketList;
    private DatabaseReference mDatabase;
    private AutoCompleteTextView etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tickets);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        etSearch = findViewById(R.id.etSearch);
        rvAllTickets = findViewById(R.id.rvAllTickets);
        rvAllTickets.setLayoutManager(new LinearLayoutManager(this));
        
        ticketList = new ArrayList<>();
        fullTicketList = new ArrayList<>();
        
        ticketAdapter = new TicketAdapter(ticketList, complaint -> {
            Intent intent = new Intent(AllTicketsActivity.this, ComplaintDetailsActivity.class);
            intent.putExtra("id", complaint.id);
            intent.putExtra("userId", complaint.userId);
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
        
        rvAllTickets.setAdapter(ticketAdapter);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadAllTickets();
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
        if (text == null || text.trim().isEmpty()) {
            filteredList.addAll(fullTicketList);
        } else {
            String query = text.toLowerCase().trim();
            for (Complaint item : fullTicketList) {
                if ((item.category != null && item.category.toLowerCase().contains(query)) ||
                    (item.description != null && item.description.toLowerCase().contains(query)) ||
                    (item.roomNumber != null && item.roomNumber.toLowerCase().contains(query)) ||
                    (item.id != null && item.id.toLowerCase().contains(query))) {
                    filteredList.add(item);
                }
            }
        }
        ticketList.clear();
        ticketList.addAll(filteredList);
        ticketAdapter.notifyDataSetChanged();
    }

    private void loadAllTickets() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullTicketList.clear();
                for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                    Complaint ticket = ticketSnapshot.getValue(Complaint.class);
                    if (ticket != null) {
                        if (ticket.id == null) ticket.id = ticketSnapshot.getKey();
                        fullTicketList.add(ticket);
                    }
                }
                Collections.sort(fullTicketList, (c1, c2) -> Long.compare(c2.getTimestampLong(), c1.getTimestampLong()));
                filter(etSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AllTicketsActivity.this, "Error loading tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
