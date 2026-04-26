package com.example.carecrew;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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

public class AvailableTicketsActivity extends AppCompatActivity {

    private RecyclerView rvAvailableTickets;
    private AvailableTicketAdapter adapter;
    private List<Complaint> allTickets;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private MaterialButton btnFilterAll, btnFilterPlumbing, btnFilterCarpentry, btnFilterElectrical, btnFilterHousekeeping, btnFilterOther, btnFilterUrgent;
    private String currentFilter = "All";
    private String staffCategory = null;

    private TextView tvSubtitle, tvTitle;
    private View btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_tickets);

        // Entrance Animation
        View mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {
            mainLayout.setAlpha(0f);
            mainLayout.animate().alpha(1f).setDuration(500).start();
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        rvAvailableTickets = findViewById(R.id.rvAvailableTickets);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvTitle = findViewById(R.id.tvTitle);
        btnBack = findViewById(R.id.btnBack);
        rvAvailableTickets.setLayoutManager(new LinearLayoutManager(this));

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        allTickets = new ArrayList<>();
        adapter = new AvailableTicketAdapter(this, new ArrayList<>(), new AvailableTicketAdapter.OnTicketActionListener() {
            @Override
            public void onAccept(Complaint ticket) {
                acceptTicket(ticket);
            }

            @Override
            public void onView(Complaint ticket) {
                viewTicket(ticket);
            }
        });
        rvAvailableTickets.setAdapter(adapter);

        initFilters();
        fetchStaffCategoryAndTickets();
        setupBottomNav();
    }

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

    private void fetchStaffCategoryAndTickets() {
        if (mAuth.getCurrentUser() == null) return;
        String email = mAuth.getCurrentUser().getEmail();
        String emailKey = email != null ? email.replace(".", ",") : "unknown";

        mDatabase.child("Users").child(emailKey).child("category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    staffCategory = snapshot.getValue(String.class);
                    if (tvSubtitle != null && staffCategory != null) {
                        tvSubtitle.setText(staffCategory);
                    }
                    updateCategoryButtonsVisibility();
                    fetchTickets();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateCategoryButtonsVisibility() {
        // Show "All", "Urgent", and the staff's own category
        btnFilterAll.setVisibility(View.VISIBLE);
        btnFilterUrgent.setVisibility(View.VISIBLE);

        btnFilterPlumbing.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Plumbing") ? View.VISIBLE : View.GONE);
        btnFilterCarpentry.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Carpentry") ? View.VISIBLE : View.GONE);
        btnFilterElectrical.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Electrical") ? View.VISIBLE : View.GONE);
        btnFilterHousekeeping.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Housekeeping") ? View.VISIBLE : View.GONE);
        btnFilterOther.setVisibility(staffCategory != null && staffCategory.equalsIgnoreCase("Other") ? View.VISIBLE : View.GONE);

        // Default to "All"
        currentFilter = "All";
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        updateFilterButtonStyles();
        
        List<Complaint> filteredList = new ArrayList<>();
        for (Complaint ticket : allTickets) {
            boolean matchesCategory = filter.equals("All") || ticket.category.equalsIgnoreCase(filter);
            boolean isUrgent = "Urgent".equalsIgnoreCase(ticket.priority) || "High".equalsIgnoreCase(ticket.priority);
            boolean matchesUrgent = filter.equals("Urgent") && isUrgent;
            
            if (filter.equals("Urgent")) {
                if (matchesUrgent) filteredList.add(ticket);
            } else {
                if (matchesCategory) filteredList.add(ticket);
            }
        }
        
        if (tvTitle != null) {
            tvTitle.setText("Available (" + filteredList.size() + ")");
        }

        adapter.updateList(filteredList);
    }

    private void updateFilterButtonStyles() {
        setUnselectedStyle(btnFilterAll, "#64748B", "#E2E8F0");
        setUnselectedStyle(btnFilterUrgent, "#7B1FA2", "#E1BEE7");
        setUnselectedStyle(btnFilterPlumbing, "#8D6E63", "#FFCCBC");
        setUnselectedStyle(btnFilterCarpentry, "#6D4C41", "#D7CCC8");
        setUnselectedStyle(btnFilterElectrical, "#4CAF50", "#C8E6C9");
        setUnselectedStyle(btnFilterHousekeeping, "#00796B", "#B2DFDB");
        setUnselectedStyle(btnFilterOther, "#455A64", "#CFD8DC");

        MaterialButton activeBtn = null;
        if (currentFilter.equals("Urgent")) activeBtn = btnFilterUrgent;
        else if (currentFilter.equalsIgnoreCase("Plumbing")) activeBtn = btnFilterPlumbing;
        else if (currentFilter.equalsIgnoreCase("Carpentry")) activeBtn = btnFilterCarpentry;
        else if (currentFilter.equalsIgnoreCase("Electrical")) activeBtn = btnFilterElectrical;
        else if (currentFilter.equalsIgnoreCase("Housekeeping")) activeBtn = btnFilterHousekeeping;
        else if (currentFilter.equalsIgnoreCase("Other")) activeBtn = btnFilterOther;
        else activeBtn = btnFilterAll;
        
        if (activeBtn != null) {
            activeBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1976D2")));
            activeBtn.setTextColor(android.graphics.Color.WHITE);
            activeBtn.setStrokeWidth(0);
        }
    }

    private void setUnselectedStyle(MaterialButton btn, String textColor, String strokeColor) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        btn.setTextColor(android.graphics.Color.parseColor(textColor));
        btn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(strokeColor)));
        btn.setStrokeWidth(2);
    }

    private void fetchTickets() {
        mDatabase.child("complaints").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTickets.clear();
                String staffEmail = mAuth.getCurrentUser().getEmail();
                String emailKey = staffEmail != null ? staffEmail.replace(".", ",") : "unknown";

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Complaint ticket = ds.getValue(Complaint.class);
                    if (ticket != null) {
                        if (ticket.id == null) ticket.id = ds.getKey();
                        
                        // Filter by staff category
                        if (staffCategory != null && !staffCategory.equalsIgnoreCase(ticket.category)) {
                            continue;
                        }

                        boolean isRejected = ds.child("rejectedBy").hasChild(emailKey);
                        boolean isUnassigned = ticket.assignedTo == null || ticket.assignedTo.isEmpty();
                        boolean isAvailableStatus = "Open".equalsIgnoreCase(ticket.status) || 
                                                 "Pending".equalsIgnoreCase(ticket.status) ||
                                                 "Cancelled".equalsIgnoreCase(ticket.status);

                        if (!isRejected && isUnassigned && isAvailableStatus) {
                            allTickets.add(ticket);
                        }
                    }
                }
                
                // Sort: Urgent/High priority on top
                allTickets.sort((t1, t2) -> {
                    boolean u1 = "Urgent".equalsIgnoreCase(t1.priority) || "High".equalsIgnoreCase(t1.priority);
                    boolean u2 = "Urgent".equalsIgnoreCase(t2.priority) || "High".equalsIgnoreCase(t2.priority);
                    if (u1 && !u2) return -1;
                    if (!u1 && u2) return 1;
                    return 0;
                });

                applyFilter(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void acceptTicket(Complaint ticket) {
        if (mAuth.getCurrentUser() == null) return;
        String staffEmail = mAuth.getCurrentUser().getEmail();
        
        mDatabase.child("complaints").child(ticket.id).child("status").setValue("Assigned");
        mDatabase.child("complaints").child(ticket.id).child("assignedTo").setValue(staffEmail)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ticket Accepted!", Toast.LENGTH_SHORT).show();
                });
    }

    private void viewTicket(Complaint ticket) {
        android.content.Intent intent = new android.content.Intent(this, TicketDetailsStaffActivity.class);
        intent.putExtra("ticketId", ticket.id);
        intent.putExtra("category", ticket.category);
        intent.putExtra("location", getString(R.string.ticket_room_format, (ticket.roomNumber != null ? ticket.roomNumber : getString(R.string.not_available))));
        intent.putExtra("priority", ticket.priority);
        intent.putExtra("description", ticket.description);
        intent.putExtra("imageUrl", ticket.imageUrl);
        startActivity(intent);
    }

    private void setupBottomNav() {
        // Handled via grid or other nav
    }
}