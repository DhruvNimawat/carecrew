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
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.RatingBar;
import com.google.android.material.textfield.TextInputEditText;

public class MyTicketsActivity extends AppCompatActivity {

    private RecyclerView ticketsRecyclerView;
    private TicketAdapter ticketAdapter;
    private List<Complaint> allTickets;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private View tabIndicator;
    private MaterialButton btnAll, btnPending, btnInProgress, btnCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints");

        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView);
        ticketsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        allTickets = new ArrayList<>();
        ticketAdapter = new TicketAdapter(allTickets, this::onTicketClicked);
        ticketsRecyclerView.setAdapter(ticketAdapter);

        tabIndicator = findViewById(R.id.tabIndicator);
        setupFilterButtons();
        fetchTickets();
        applyEntranceAnimations();

        // Position indicator initially on "All"
        btnAll.post(() -> {
            moveIndicator(btnAll, false);
            btnAll.setTextColor(getResources().getColor(R.color.primary_blue));
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void moveIndicator(MaterialButton target, boolean animate) {
        if (target == null || tabIndicator == null) return;

        target.post(() -> {
            float textWidth = target.getPaint().measureText(target.getText().toString());
            
            // Add extra horizontal padding specifically for the "All" tab to make it slightly wider
            int horizontalPadding = (target.getId() == R.id.tabAll) ? 120 : 48;
            
            int indicatorWidth = (int) textWidth + horizontalPadding;
            int indicatorHeight = (int) (target.getHeight() * 0.75f);

            float targetX = target.getX() + (target.getWidth() - indicatorWidth) / 2f;
            float targetY = target.getY() + (target.getHeight() - indicatorHeight) / 2f;

            if (animate) {
                tabIndicator.animate()
                        .x(targetX)
                        .y(targetY)
                        .setDuration(300)
                        .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                        .start();

                android.animation.ValueAnimator widthAnim = android.animation.ValueAnimator.ofInt(tabIndicator.getWidth(), indicatorWidth);
                widthAnim.addUpdateListener(animation -> {
                    tabIndicator.getLayoutParams().width = (int) animation.getAnimatedValue();
                    tabIndicator.requestLayout();
                });
                widthAnim.setDuration(300);
                widthAnim.start();

                android.animation.ValueAnimator heightAnim = android.animation.ValueAnimator.ofInt(tabIndicator.getHeight(), indicatorHeight);
                heightAnim.addUpdateListener(animation -> {
                    tabIndicator.getLayoutParams().height = (int) animation.getAnimatedValue();
                    tabIndicator.requestLayout();
                });
                heightAnim.setDuration(300);
                heightAnim.start();
            } else {
                tabIndicator.setX(targetX);
                tabIndicator.setY(targetY);
                tabIndicator.getLayoutParams().width = indicatorWidth;
                tabIndicator.getLayoutParams().height = indicatorHeight;
                tabIndicator.requestLayout();
            }
        });
    }

    private void applyEntranceAnimations() {
        View header = findViewById(R.id.headerBackground);
        View title = findViewById(R.id.screenTitle);
        View tabs = findViewById(R.id.tabsScroll);
        
        header.setTranslationY(-200f);
        header.animate().translationY(0).setDuration(600).start();

        title.setAlpha(0f);
        title.setTranslationX(-50f);
        title.animate().alpha(1f).translationX(0).setDuration(500).setStartDelay(200).start();

        if (tabs != null) {
            tabs.setAlpha(0f);
            tabs.setTranslationY(50f);
            tabs.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(400).start();
        }
    }

    private void onTicketClicked(Complaint complaint) {
        Intent intent = new Intent(this, ComplaintDetailsActivity.class);
        intent.putExtra("id", complaint.id);
        intent.putExtra("category", complaint.category);
        intent.putExtra("status", complaint.status);
        intent.putExtra("description", complaint.description);
        intent.putExtra("block", complaint.block);
        intent.putExtra("floor", complaint.floor);
        intent.putExtra("roomNumber", complaint.roomNumber);
        intent.putExtra("priority", complaint.priority);
        intent.putExtra("assignedTo", complaint.assignedTo);
        intent.putExtra("userId", complaint.userId);
        intent.putExtra("afterRepairImageUrl", complaint.afterRepairImageUrl);
        intent.putExtra("startWorkImageUrl", complaint.startWorkImageUrl);
        intent.putExtra("imageUrl", complaint.imageUrl);

        // Pass timestamp as string
        String ts = "N/A";
        if (complaint.timestamp != null) {
            ts = complaint.timestamp.toString();
        }
        intent.putExtra("timestamp", ts);

        startActivity(intent);
    }

    private void setupFilterButtons() {
        btnAll = findViewById(R.id.tabAll);
        btnPending = findViewById(R.id.tabPending);
        btnInProgress = findViewById(R.id.tabInProgress);
        btnCompleted = findViewById(R.id.tabCompleted);

        btnAll.setOnClickListener(v -> filterTickets("All", btnAll));
        btnPending.setOnClickListener(v -> filterTickets("Pending", btnPending));
        btnInProgress.setOnClickListener(v -> filterTickets("In Progress", btnInProgress));
        btnCompleted.setOnClickListener(v -> filterTickets("Completed", btnCompleted));
    }

    private void fetchTickets() {
        String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : null;
        String currentUserId = email != null ? email.replace(".", ",") : "anonymous";
        
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTickets.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Complaint ticket = postSnapshot.getValue(Complaint.class);
                    if (ticket != null && currentUserId.equals(ticket.userId)) {
                        ticket.id = postSnapshot.getKey(); // Ensure ID is set
                        allTickets.add(ticket);
                    }
                }
                // Sort tickets: newest first (descending order of timestamp)
                Collections.sort(allTickets, (t1, t2) -> Long.compare(t2.getTimestampLong(), t1.getTimestampLong()));

                ticketAdapter.updateList(allTickets);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyTicketsActivity.this, "Failed to load tickets", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterTickets(String status, MaterialButton clickedButton) {
        moveIndicator(clickedButton, true);
        resetButtonStyles();
        clickedButton.setTextColor(getResources().getColor(R.color.primary_blue));
        
        List<Complaint> filteredList = new ArrayList<>();
        if ("All".equals(status)) {
            filteredList = allTickets;
        } else {
            for (Complaint t : allTickets) {
                if (status.equalsIgnoreCase(t.status)) {
                    filteredList.add(t);
                }
            }
        }
        ticketAdapter.updateList(filteredList);
    }

    private void resetButtonStyles() {
        MaterialButton[] buttons = {btnAll, btnPending, btnInProgress, btnCompleted};
        for (MaterialButton b : buttons) {
            b.setTextColor(android.graphics.Color.parseColor("#CCFFFFFF"));
        }
    }
}