package com.example.carecrew;

import android.os.Bundle;
import android.widget.ImageView;
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

public class RecentUpdatesActivity extends AppCompatActivity {

    private RecyclerView rvUpdates;
    private RecentUpdatesAdapter adapter;
    private ImageView btnBack;
    private DatabaseReference mDatabase;
    private List<UpdateItem> updatesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_updates);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvUpdates = findViewById(R.id.rvRecentUpdates);
        rvUpdates.setLayoutManager(new LinearLayoutManager(this));
        
        updatesList = new ArrayList<>();
        adapter = new RecentUpdatesAdapter(updatesList);
        rvUpdates.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");
        
        listenForRealtimeUpdates();
    }

    private void listenForRealtimeUpdates() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updatesList.clear();
                List<Complaint> complaints = new ArrayList<>();
                
                for (DataSnapshot data : snapshot.getChildren()) {
                    Complaint complaint = data.getValue(Complaint.class);
                    if (complaint != null) {
                        complaints.add(complaint);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(complaints, (c1, c2) -> 
                    Long.compare(c2.getTimestampLong(), c1.getTimestampLong()));

                // Take the top 15 most recent updates
                int limit = Math.min(complaints.size(), 15);
                for (int i = 0; i < limit; i++) {
                    Complaint c = complaints.get(i);
                    updatesList.add(convertToUpdateItem(c));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private UpdateItem convertToUpdateItem(Complaint c) {
        String message;
        UpdateItem.UpdateType type;
        
        String statusStr = c.status != null ? c.status.toLowerCase() : "";
        
        if (statusStr.equals("resolved") || statusStr.equals("completed")) {
            message = "Complaint #" + c.roomNumber + " resolved";
            type = UpdateItem.UpdateType.RESOLVED;
        } else if (statusStr.equals("in progress") || statusStr.equals("assigned")) {
            message = "Ticket for Room " + c.roomNumber + " is now In-Progress";
            type = UpdateItem.UpdateType.IN_PROGRESS;
        } else if ("high".equalsIgnoreCase(c.priority)) {
            message = "Urgent: High priority issue in Room " + c.roomNumber;
            type = UpdateItem.UpdateType.URGENT;
        } else {
            message = "New complaint registered for Room " + c.roomNumber;
            type = UpdateItem.UpdateType.ANNOUNCEMENT;
        }

        return new UpdateItem(message, c.timestamp, type);
    }
}