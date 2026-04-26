package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import com.google.android.material.button.MaterialButton;

public class StaffHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryTicketAdapter adapter;
    private List<Complaint> historyList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String staffCategory = null;

    private View navHome, navProfile;
    private TextView tvStaffGreeting, tvSubtitle, tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_history);

        // Entrance Animation
        View mainLayout = findViewById(R.id.main);
        mainLayout.setAlpha(0f);
        mainLayout.animate().alpha(1f).setDuration(500).start();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        
        historyList = new ArrayList<>();
        adapter = new HistoryTicketAdapter(this, historyList);
        rvHistory.setAdapter(adapter);

        fetchStaffNameAndCategory();
        fetchHistoryJobs();
    }

    private void fetchStaffNameAndCategory() {
        if (mAuth.getCurrentUser() != null) {
            String email = mAuth.getCurrentUser().getEmail();
            String emailKey = email != null ? email.replace(".", ",") : "unknown";
            FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        staffCategory = snapshot.child("category").getValue(String.class);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void fetchHistoryJobs() {
        if (mAuth.getCurrentUser() == null) return;
        String staffEmail = mAuth.getCurrentUser().getEmail();
        String emailKey = staffEmail != null ? staffEmail.replace(".", ",") : "unknown";

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint ticket = ds.getValue(Complaint.class);
                    if (ticket != null) {
                        // 1. Completed Jobs (Assigned to me AND status is Completed/Resolved)
                        boolean isCompletedByMe = ("Completed".equalsIgnoreCase(ticket.status) || "Resolved".equalsIgnoreCase(ticket.status)) &&
                                                 staffEmail != null && staffEmail.equalsIgnoreCase(ticket.assignedTo);

                        // 2. Cancelled Jobs (Status is Cancelled AND I was the one who cancelled it)
                        boolean isCancelledByMe = "Cancelled".equalsIgnoreCase(ticket.status) &&
                                                 staffEmail != null && staffEmail.equalsIgnoreCase(ticket.cancelledBy);

                        if (isCompletedByMe || isCancelledByMe) {
                            if (ticket.id == null) ticket.id = ds.getKey();
                            historyList.add(ticket);
                        }
                    }
                }
                
                // Update UI
                if (adapter != null) {
                    adapter.updateList(historyList);
                }
                
                // Update Header with count and Category
                if (tvTitle != null) {
                    tvTitle.setText("Work History (" + historyList.size() + ")");
                }
                
                // Fetch and set category in subtitle
                FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey).child("category")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            if (snap.exists()) {
                                staffCategory = snap.getValue(String.class);
                                if (tvSubtitle != null) tvSubtitle.setText(staffCategory);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StaffHistoryActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String currentFilter = "All";
    private MaterialButton btnFilterAll, btnFilterPlumbing, btnFilterCarpentry, btnFilterElectrical, btnFilterHousekeeping, btnFilterOther, btnFilterUrgent;

    private void initFilters() {
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUrgent = findViewById(R.id.btnFilterUrgent);
        btnFilterPlumbing = findViewById(R.id.btnFilterPlumbing);
        btnFilterCarpentry = findViewById(R.id.btnFilterCarpentry);
        btnFilterElectrical = findViewById(R.id.btnFilterElectrical);
        btnFilterHousekeeping = findViewById(R.id.btnFilterHousekeeping);
        btnFilterOther = findViewById(R.id.btnFilterOther);

        btnFilterAll.setOnClickListener(v -> applyFilter("All"));
        btnFilterUrgent.setOnClickListener(v -> applyFilter("Urgent"));
        btnFilterPlumbing.setOnClickListener(v -> applyFilter("Plumbing"));
        btnFilterCarpentry.setOnClickListener(v -> applyFilter("Carpentry"));
        btnFilterElectrical.setOnClickListener(v -> applyFilter("Electrical"));
        btnFilterHousekeeping.setOnClickListener(v -> applyFilter("Housekeeping"));
        btnFilterOther.setOnClickListener(v -> applyFilter("Other"));
    }

    private void updateCategoryButtonsVisibility() {
        if (btnFilterAll == null) return;
        btnFilterAll.setVisibility(View.VISIBLE);
        btnFilterUrgent.setVisibility(View.VISIBLE);
        btnFilterPlumbing.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Plumbing") ? View.VISIBLE : View.GONE);
        btnFilterCarpentry.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Carpentry") ? View.VISIBLE : View.GONE);
        btnFilterElectrical.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Electrical") ? View.VISIBLE : View.GONE);
        btnFilterHousekeeping.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Housekeeping") ? View.VISIBLE : View.GONE);
        btnFilterOther.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Other") ? View.VISIBLE : View.GONE);
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateFilterButtonStyles();
        List<Complaint> filtered = new ArrayList<>();
        for (Complaint c : historyList) {
            boolean matchesCategory = filter.equals("All") || c.category.equalsIgnoreCase(filter);
            boolean matchesUrgent = filter.equals("Urgent") && ("Urgent".equalsIgnoreCase(c.priority) || "High".equalsIgnoreCase(c.priority));
            if (filter.equals("Urgent")) {
                if (matchesUrgent) filtered.add(c);
            } else {
                if (matchesCategory) filtered.add(c);
            }
        }
        adapter.updateList(filtered);
    }

    private void updateFilterButtonStyles() {
        setUnselectedStyle(btnFilterAll, "#64748B", "#E2E8F0");
        setUnselectedStyle(btnFilterUrgent, "#7B1FA2", "#E1BEE7");
        setUnselectedStyle(btnFilterPlumbing, "#64748B", "#E2E8F0");
        setUnselectedStyle(btnFilterCarpentry, "#64748B", "#E2E8F0");
        setUnselectedStyle(btnFilterElectrical, "#64748B", "#E2E8F0");
        setUnselectedStyle(btnFilterHousekeeping, "#64748B", "#E2E8F0");
        setUnselectedStyle(btnFilterOther, "#64748B", "#E2E8F0");

        MaterialButton activeBtn;
        switch (currentFilter) {
            case "Urgent": activeBtn = btnFilterUrgent; break;
            case "Plumbing": activeBtn = btnFilterPlumbing; break;
            case "Carpentry": activeBtn = btnFilterCarpentry; break;
            case "Electrical": activeBtn = btnFilterElectrical; break;
            case "Housekeeping": activeBtn = btnFilterHousekeeping; break;
            case "Other": activeBtn = btnFilterOther; break;
            default: activeBtn = btnFilterAll; break;
        }
        activeBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1976D2")));
        activeBtn.setTextColor(android.graphics.Color.WHITE);
        activeBtn.setStrokeWidth(0);
    }

    private void setUnselectedStyle(MaterialButton btn, String textColor, String strokeColor) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        btn.setTextColor(android.graphics.Color.parseColor(textColor));
        btn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(strokeColor)));
        btn.setStrokeWidth(2);
    }
}