package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecentUpdatesActivity extends AppCompatActivity {

    private RecyclerView rvRecentUpdates;
    private UpdateAdapter adapter;
    private List<Complaint> updateList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private TextView tvNoUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_updates);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        rvRecentUpdates = findViewById(R.id.rvRecentUpdates);
        tvNoUpdates = findViewById(R.id.tvNoUpdates);
        
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        updateList = new ArrayList<>();
        adapter = new UpdateAdapter(updateList);
        rvRecentUpdates.setLayoutManager(new LinearLayoutManager(this));
        rvRecentUpdates.setAdapter(adapter);

        fetchUpdates();
    }

    private void fetchUpdates() {
        if (mAuth.getCurrentUser() == null) return;
        String email = mAuth.getCurrentUser().getEmail();
        String userId = email != null ? email.replace(".", ",") : "anonymous";

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Complaint complaint = postSnapshot.getValue(Complaint.class);
                    if (complaint != null && userId.equals(complaint.userId)) {
                        updateList.add(complaint);
                    }
                }
                // Sort by timestamp descending
                Collections.sort(updateList, (c1, c2) -> Long.compare(c2.getTimestampLong(), c1.getTimestampLong()));
                
                if (updateList.isEmpty()) {
                    tvNoUpdates.setVisibility(View.VISIBLE);
                } else {
                    tvNoUpdates.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}