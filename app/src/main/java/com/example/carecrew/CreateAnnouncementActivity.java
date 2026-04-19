package com.example.carecrew;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etMessage;
    private MaterialButton btnPublish;
    private ImageButton btnBack;
    private RecyclerView rvAnnouncements;
    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_announcement);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        btnPublish = findViewById(R.id.btnPublish);
        btnBack = findViewById(R.id.btnBack);
        rvAnnouncements = findViewById(R.id.rvAnnouncements);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();

        btnPublish.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            publishAnnouncement(title, message);
        });

        fetchAnnouncements();
    }

    private void setupRecyclerView() {
        announcementList = new ArrayList<>();
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnnouncementAdapter(announcementList, announcement -> {
            mDatabase.child("Announcements").child(announcement.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Announcement Completed", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        rvAnnouncements.setAdapter(adapter);
    }

    private void fetchAnnouncements() {
        mDatabase.child("Announcements").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                announcementList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        // Check if it's an object, not a legacy string
                        if (postSnapshot.getValue() instanceof java.util.Map) {
                            Announcement announcement = postSnapshot.getValue(Announcement.class);
                            if (announcement != null && announcement.getId() != null) {
                                announcementList.add(announcement);
                            }
                        } else {
                            // Optionally delete legacy data to prevent future crashes
                            postSnapshot.getRef().removeValue();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Collections.sort(announcementList, (a1, a2) -> Long.compare(a2.getTimestamp(), a1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void publishAnnouncement(String title, String message) {
        String id = mDatabase.child("Announcements").push().getKey();
        Map<String, Object> announcement = new HashMap<>();
        announcement.put("id", id);
        announcement.put("title", title);
        announcement.put("message", message);
        announcement.put("timestamp", System.currentTimeMillis());

        if (id != null) {
            mDatabase.child("Announcements").child(id).setValue(announcement)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Announcement Published!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}