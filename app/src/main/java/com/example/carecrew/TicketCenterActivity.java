package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

public class TicketCenterActivity extends AppCompatActivity {

    private RecyclerView rvTicketHistory;
    private HistoryTicketAdapter adapter;
    private List<Complaint> historyList, fullHistoryList;
    private View navHome, navHistory, navProfile;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String currentFilter = "All";
    private String userRole = "User";
    private MaterialButton btnFilterAll, btnFilterUrgent, btnFilterPlumbing, btnFilterCarpentry, btnFilterElectrical, btnFilterHousekeeping, btnFilterOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_center);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        if (getIntent() != null && getIntent().hasExtra("role")) {
            userRole = getIntent().getStringExtra("role");
        }

        // Initialize Views
        rvTicketHistory = findViewById(R.id.rvTicketHistory);
        rvTicketHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();
        fullHistoryList = new ArrayList<>();
        adapter = new HistoryTicketAdapter(this, historyList);
        rvTicketHistory.setAdapter(adapter);

        navHome = findViewById(R.id.navHome);
        navHistory = findViewById(R.id.navHistory);
        navProfile = findViewById(R.id.navProfile);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUrgent = findViewById(R.id.btnFilterUrgent);
        btnFilterPlumbing = findViewById(R.id.btnFilterPlumbing);
        btnFilterCarpentry = findViewById(R.id.btnFilterCarpentry);
        btnFilterElectrical = findViewById(R.id.btnFilterElectrical);
        btnFilterHousekeeping = findViewById(R.id.btnFilterHousekeeping);
        btnFilterOther = findViewById(R.id.btnFilterOther);

        setupFilters();
        setupBottomNav();
        fetchTickets();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(v -> filterHistory("All"));
        btnFilterUrgent.setOnClickListener(v -> filterHistory("Urgent"));
        btnFilterPlumbing.setOnClickListener(v -> filterHistory("Plumbing"));
        btnFilterCarpentry.setOnClickListener(v -> filterHistory("Carpentry"));
        btnFilterElectrical.setOnClickListener(v -> filterHistory("Electrical"));
        btnFilterHousekeeping.setOnClickListener(v -> filterHistory("Housekeeping"));
        btnFilterOther.setOnClickListener(v -> filterHistory("Other"));
    }

    private void filterHistory(String filter) {
        currentFilter = filter;
        historyList.clear();
        if ("All".equals(filter)) {
            historyList.addAll(fullHistoryList);
        } else if ("Urgent".equals(filter)) {
            for (Complaint c : fullHistoryList) {
                if ("Urgent".equalsIgnoreCase(c.priority)) historyList.add(c);
            }
        } else {
            for (Complaint c : fullHistoryList) {
                if (filter.equalsIgnoreCase(c.category)) historyList.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> {
            Intent intent;
            if ("Admin".equalsIgnoreCase(userRole)) {
                intent = new Intent(this, AdminDashboard.class);
            } else if ("Warden".equalsIgnoreCase(userRole)) {
                intent = new Intent(this, WardenDashboard.class);
            } else {
                intent = new Intent(this, UserDashboardActivity.class);
            }
            startActivity(intent);
            finish();
        });
        navHistory.setOnClickListener(v -> {
            // Already here
        });
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("role", userRole);
            startActivity(intent);
            finish();
        });
    }

    private void fetchTickets() {
        if (mAuth.getCurrentUser() == null) return;
        String userEmail = mAuth.getCurrentUser().getEmail();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullHistoryList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint complaint = ds.getValue(Complaint.class);
                    if (complaint == null) continue;

                    if ("Admin".equalsIgnoreCase(userRole) || "Warden".equalsIgnoreCase(userRole)) {
                        fullHistoryList.add(complaint);
                    } else if (userEmail != null && userEmail.equalsIgnoreCase(complaint.userId)) {
                        fullHistoryList.add(complaint);
                    }
                }
                filterHistory(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TicketCenterActivity.this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
