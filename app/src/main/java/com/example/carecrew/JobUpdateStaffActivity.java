package com.example.carecrew;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JobUpdateStaffActivity extends AppCompatActivity {

    private String ticketId;
    private TextView tvTicketId, tvUserName, tvRoom, tvStatusBadge;
    private android.widget.ImageView ivStartWorkPhoto;
    private android.widget.LinearLayout layoutStartWorkPhoto;
    private TextView tvTimelineAccepted, tvTimelineStarted, tvTimelineInProgress, tvTimelineCompleted;
    private View dotAccepted, dotStarted, dotInProgress, dotCompleted, notificationBanner;
    private MaterialButton btnStartWork, btnMarkInProgress, btnComplete, btnCancel, btnBannerCamera;
    private DatabaseReference mDatabase;
    private boolean isBeforePhotoCaptured = false;
    
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private Uri photoURI;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_update_staff);

        // Entrance animation
        View rootView = findViewById(android.R.id.content);
        rootView.setAlpha(0f);
        rootView.animate().alpha(1f).setDuration(500).start();

        ticketId = getIntent().getStringExtra("ticketId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints").child(ticketId);

        tvTicketId = findViewById(R.id.tvTicketId);
        tvUserName = findViewById(R.id.tvUserName);
        tvRoom = findViewById(R.id.tvRoom);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        ivStartWorkPhoto = findViewById(R.id.ivStartWorkPhoto);
        layoutStartWorkPhoto = findViewById(R.id.layoutStartWorkPhoto);

        tvTimelineAccepted = findViewById(R.id.tvTimelineAccepted);
        tvTimelineStarted = findViewById(R.id.tvTimelineStarted);
        tvTimelineInProgress = findViewById(R.id.tvTimelineInProgress);
        tvTimelineCompleted = findViewById(R.id.tvTimelineCompleted);

        dotAccepted = findViewById(R.id.dotAccepted);
        dotStarted = findViewById(R.id.dotStarted);
        dotInProgress = findViewById(R.id.dotInProgress);
        dotCompleted = findViewById(R.id.dotCompleted);

        btnStartWork = findViewById(R.id.btnStartWork);
        btnMarkInProgress = findViewById(R.id.btnMarkInProgress);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
        notificationBanner = findViewById(R.id.notificationBanner);
        btnBannerCamera = findViewById(R.id.btnBannerCamera);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadTicketDetails();
        setupClickListeners();
    }

    private void loadTicketDetails() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint ticket = snapshot.getValue(Complaint.class);
                if (ticket != null) {
                    tvTicketId.setText(getString(R.string.ticket_id_prefix, ticketId.substring(Math.max(0, ticketId.length() - 6))));
                    tvUserName.setText(ticket.userName != null ? ticket.userName : getString(R.string.not_available));
                    tvRoom.setText(getString(R.string.ticket_room_format, ticket.roomNumber));
                    tvStatusBadge.setText(ticket.status);
                    
                    isBeforePhotoCaptured = (ticket.startWorkImageUrl != null);
                    if (isBeforePhotoCaptured) {
                        layoutStartWorkPhoto.setVisibility(View.VISIBLE);
                        com.bumptech.glide.Glide.with(JobUpdateStaffActivity.this)
                                .load(ticket.startWorkImageUrl)
                                .into(ivStartWorkPhoto);
                    } else {
                        layoutStartWorkPhoto.setVisibility(View.GONE);
                    }
                    
                    updateTimeline(ticket.status);
                    updateButtonStyles(ticket.status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateTimeline(String status) {
        int activeColor = 0xFF10B981; // Green
        int inactiveColor = 0xFFE2E8F0; // Gray
        int activeTextColor = 0xFF1E293B; // Darker/Bold color
        int inactiveTextColor = 0xFF94A3B8; // Light Gray

        // Reset all
        dotAccepted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        dotStarted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        dotInProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));
        dotCompleted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveColor));

        tvTimelineAccepted.setTextColor(inactiveTextColor);
        tvTimelineStarted.setTextColor(inactiveTextColor);
        tvTimelineInProgress.setTextColor(inactiveTextColor);
        tvTimelineCompleted.setTextColor(inactiveTextColor);

        // Always show Accepted as active or at least reachable
        dotAccepted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
        tvTimelineAccepted.setTextColor(activeTextColor);

        if ("Started".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            dotStarted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            tvTimelineStarted.setTextColor(activeTextColor);
        }
        if ("In Progress".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            dotInProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            tvTimelineInProgress.setTextColor(activeTextColor);
        }
        if ("Completed".equalsIgnoreCase(status)) {
            dotCompleted.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            tvTimelineCompleted.setTextColor(activeTextColor);
        }
    }

    private void setupClickListeners() {
        btnStartWork.setOnClickListener(v -> {
            // Directly start work without image for now
            mDatabase.child("status").setValue("Started");
            updateButtonStyles("Started");
            Toast.makeText(this, "Work Started!", Toast.LENGTH_SHORT).show();
        });

        btnBannerCamera.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });

        btnMarkInProgress.setOnClickListener(v -> {
            mDatabase.child("status").setValue("In Progress");
            updateButtonStyles("In Progress");
        });

        btnComplete.setOnClickListener(v -> {
            updateButtonStyles("Completed");
            Intent intent = new Intent(JobUpdateStaffActivity.this, JobCompletionActivity.class);
            intent.putExtra("ticketId", ticketId);
            startActivity(intent);
        });

        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(JobUpdateStaffActivity.this, CancelJobActivity.class);
            intent.putExtra("ticketId", ticketId);
            startActivity(intent);
        });
    }

    private void updateButtonStyles(String activeStatus) {
        int activeColor = 0xFF10B981; // Green
        int defaultColor = 0xFF2563EB; // Blue

        // Reset all to default blue
        btnStartWork.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
        btnMarkInProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
        btnComplete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));

        // Enable/Disable buttons based on status to enforce workflow
        boolean isAccepted = "Accepted".equalsIgnoreCase(activeStatus) || "Assigned".equalsIgnoreCase(activeStatus) || "Pending".equalsIgnoreCase(activeStatus) || activeStatus == null;
        boolean isStarted = "Started".equalsIgnoreCase(activeStatus);
        boolean isInProgress = "In Progress".equalsIgnoreCase(activeStatus);
        boolean isCompleted = "Completed".equalsIgnoreCase(activeStatus);

        btnStartWork.setEnabled(isAccepted);
        btnMarkInProgress.setEnabled(isStarted);
        btnComplete.setEnabled(isStarted || isInProgress);
        btnCancel.setEnabled(!isCompleted);

        // Set active to green
        if (isStarted) {
            btnStartWork.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            btnStartWork.setText("Started");
        } else if (isInProgress) {
            btnMarkInProgress.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            btnMarkInProgress.setText("In Progress");
        } else if (isCompleted) {
            btnComplete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            btnComplete.setText("Completed");
            btnStartWork.setEnabled(false);
            btnMarkInProgress.setEnabled(false);
            btnComplete.setEnabled(false);
        }

        // Apply alpha to disabled buttons for better visual feedback
        btnStartWork.setAlpha(btnStartWork.isEnabled() || isStarted ? 1.0f : 0.5f);
        btnMarkInProgress.setAlpha(btnMarkInProgress.isEnabled() || isInProgress ? 1.0f : 0.5f);
        btnComplete.setAlpha(btnComplete.isEnabled() || isCompleted ? 1.0f : 0.5f);
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.carecrew.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            uploadImageAndStartWork();
        }
    }

    private void uploadImageAndStartWork() {
        if (photoURI == null) return;
        
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("work_images/" + ticketId + "_start.jpg");
        
        storageRef.putFile(photoURI).addOnSuccessListener(taskSnapshot -> {
            // Get the URL from the snapshot's storage reference to be safe
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                mDatabase.child("startWorkImageUrl").setValue(uri.toString());
                mDatabase.child("status").setValue("Started");
                isBeforePhotoCaptured = true;
                
                updateButtonStyles("Started");
                notificationBanner.setVisibility(View.GONE);

                Toast.makeText(this, "Work Started!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}