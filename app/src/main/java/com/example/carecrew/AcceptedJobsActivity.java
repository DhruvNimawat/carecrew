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

public class AcceptedJobsActivity extends AppCompatActivity {

    private RecyclerView rvAcceptedJobs;
    private AcceptedJobAdapter adapter;
    private List<Complaint> acceptedList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String staffCategory = null;

    private View navHome, navProfile;

    private TextView tvSubtitle, tvTitle;
    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_jobs);

        // Entrance Animation
        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {
            mainLayout.setAlpha(0f);
            mainLayout.animate().alpha(1f).setDuration(500).start();
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        rvAcceptedJobs = findViewById(R.id.rvAcceptedJobs);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        rvAcceptedJobs.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        acceptedList = new ArrayList<>();
        adapter = new AcceptedJobAdapter(this, new ArrayList<>());
        rvAcceptedJobs.setAdapter(adapter);

        initFilters();
        fetchAcceptedJobs();
    }

    private void fetchAcceptedJobs() {
        if (mAuth.getCurrentUser() == null) return;
        String staffEmail = mAuth.getCurrentUser().getEmail();
        String emailKey = staffEmail != null ? staffEmail.replace(".", ",") : "unknown";

        // Fetch staff category to show in subtitle
        FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey).child("category")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            staffCategory = snapshot.getValue(String.class);
                            if (tvSubtitle != null) {
                                tvSubtitle.setText(staffCategory);
                            }
                            if (tvTitle != null && acceptedList != null) {
                                tvTitle.setText("Accepted Jobs (" + acceptedList.size() + ")");
                            }
                            updateCategoryButtonsVisibility();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                acceptedList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint ticket = ds.getValue(Complaint.class);
                    if (ticket != null && staffEmail != null && staffEmail.equalsIgnoreCase(ticket.assignedTo)) {
                        // Only show active jobs: Assigned, Started, or In Progress
                        boolean isActive = "Assigned".equalsIgnoreCase(ticket.status) || 
                                         "Started".equalsIgnoreCase(ticket.status) || 
                                         "In Progress".equalsIgnoreCase(ticket.status);
                        
                        if (isActive) {
                            if (ticket.id == null) ticket.id = ds.getKey();
                            acceptedList.add(ticket);
                        }
                    }
                }
                
                // Update header count based on the current list
                if (tvTitle != null) {
                    tvTitle.setText("Accepted Jobs (" + acceptedList.size() + ")");
                }

                applyFilter(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AcceptedJobsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        for (Complaint c : acceptedList) {
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