package com.example.carecrew;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
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

public class JobCompletionActivity extends AppCompatActivity {

    private String ticketId;
    private ImageView ivBefore, ivAfter;
    private DatabaseReference mDatabase;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int CAMERA_PERMISSION_CODE = 101;
    private Uri photoURI;
    private String currentPhotoPath;
    private boolean isAfterPhotoUploaded = false;
    private String uploadedImageUrl;
    private com.google.firebase.storage.UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_completion);

        if (savedInstanceState != null) {
            isAfterPhotoUploaded = savedInstanceState.getBoolean("isAfterPhotoUploaded", false);
            currentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            String uriString = savedInstanceState.getString("photoURI");
            if (uriString != null) {
                photoURI = Uri.parse(uriString);
            }
        }

        ticketId = getIntent().getStringExtra("ticketId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints").child(ticketId);

        ivBefore = findViewById(R.id.ivBefore);
        ivAfter = findViewById(R.id.ivAfter);

        loadImages();

        if (isAfterPhotoUploaded && photoURI != null) {
            Glide.with(this).load(photoURI).into(ivAfter);
        } else if (photoURI != null) {
            // Captured but not necessarily uploaded yet, or upload was in progress
            Glide.with(this).load(photoURI).into(ivAfter);
        }

        findViewById(R.id.btnCaptureAfter).setOnClickListener(v -> dispatchTakePictureIntent());
        findViewById(R.id.btnUploadContinue).setOnClickListener(v -> finishJob());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isAfterPhotoUploaded", isAfterPhotoUploaded);
        outState.putString("currentPhotoPath", currentPhotoPath);
        if (photoURI != null) {
            outState.putString("photoURI", photoURI.toString());
        }
    }

    private void loadImages() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint ticket = snapshot.getValue(Complaint.class);
                if (ticket != null) {
                    // Show original user uploaded image (problem image) in Before box
                    if (ticket.imageUrl != null && !ticket.imageUrl.isEmpty()) {
                        Glide.with(JobCompletionActivity.this)
                                .load(ticket.imageUrl)
                                .placeholder(R.drawable.ic_search)
                                .into(ivBefore);
                    } else if (ticket.startWorkImageUrl != null) {
                        // Fallback to start work image if user didn't provide one
                        Glide.with(JobCompletionActivity.this)
                                .load(ticket.startWorkImageUrl)
                                .placeholder(R.drawable.ic_search)
                                .into(ivBefore);
                    }

                    if (ticket.afterRepairImageUrl != null) {
                        isAfterPhotoUploaded = true;
                        Glide.with(JobCompletionActivity.this)
                                .load(ticket.afterRepairImageUrl)
                                .placeholder(R.drawable.ic_search)
                                .into(ivAfter);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void dispatchTakePictureIntent() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (photoFile != null) {
            photoURI = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            
            // Grant permission to all apps that can handle this intent
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_after_";
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            isAfterPhotoUploaded = true; // Allow proceeding as soon as photo is taken
            uploadAfterImage();
        }
    }

    private void uploadAfterImage() {
        if (photoURI == null) return;
        
        // Show local preview immediately
        Glide.with(this)
                .load(photoURI)
                .into(ivAfter);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("work_images/" + ticketId + "_after.jpg");
        uploadTask = storageRef.putFile(photoURI);
        
        setupUploadListeners();
    }

    private void setupUploadListeners() {
        if (uploadTask == null) return;

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                uploadedImageUrl = uri.toString();
                mDatabase.child("afterRepairImageUrl").setValue(uploadedImageUrl);
                isAfterPhotoUploaded = true;
                
                if (!isFinishing()) {
                    Glide.with(JobCompletionActivity.this)
                            .load(uploadedImageUrl)
                            .placeholder(R.drawable.ic_search)
                            .signature(new com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis()))
                            .into(ivAfter);
                    Toast.makeText(JobCompletionActivity.this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                isAfterPhotoUploaded = false;
                Toast.makeText(JobCompletionActivity.this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            isAfterPhotoUploaded = false;
            if (!isFinishing()) {
                Toast.makeText(JobCompletionActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishJob() {
        // If we have a local URI or it's already in the DB, we consider it "captured"
        if (photoURI == null && !isAfterPhotoUploaded) {
            Toast.makeText(this, "Please capture an after-repair photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = ((android.widget.EditText)findViewById(R.id.etRepairNotes)).getText().toString().trim();
        if (notes.isEmpty()) {
            Toast.makeText(this, "Please enter repair notes", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if upload is still in progress OR if we are waiting for the URL/DB update
        if ((uploadTask != null && !uploadTask.isComplete()) || (photoURI != null && uploadedImageUrl == null)) {
            android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Finalizing photo upload...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            // Use a handler to check for uploadedImageUrl if uploadTask is already "complete" but success listener hasn't finished
            if (uploadTask != null && uploadTask.isComplete() && uploadedImageUrl == null) {
                new android.os.Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    if (uploadedImageUrl != null) {
                        performFinalUpdates(notes);
                    } else {
                        Toast.makeText(this, "Still processing upload, please try again", Toast.LENGTH_SHORT).show();
                    }
                }, 2000);
            } else {
                uploadTask.addOnCompleteListener(task -> {
                    // Wait slightly for the onSuccessListener (URL fetch) to complete
                    new android.os.Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful() && uploadedImageUrl != null) {
                            performFinalUpdates(notes);
                        } else if (task.isSuccessful()) {
                            // Upload done but URL still fetching? Try proceeding
                            performFinalUpdates(notes);
                        } else {
                            Toast.makeText(this, "Upload failed, please try again", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000);
                });
            }
            return;
        }

        performFinalUpdates(notes);
    }

    private void performFinalUpdates(String notes) {
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Completing job...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        long endTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String closedAt = sdf.format(new Date(endTime));

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "Completed");
        updates.put("repairNotes", notes);
        updates.put("closedAt", closedAt);
        updates.put("completionTimeMillis", endTime);
        updates.put("staffName", com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0]);
        
        // Ensure image URL is linked even if the separate setValue call was delayed
        if (uploadedImageUrl != null) {
            updates.put("afterRepairImageUrl", uploadedImageUrl);
        }

        mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            showCompletionDialog();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to complete job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showCompletionDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_job_done);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        android.widget.TextView tvSubtitle = dialog.findViewById(R.id.tvDialogSubtitle);
        if (tvSubtitle != null) {
            tvSubtitle.setText(getString(R.string.label_task_completed));
        }

        dialog.setCancelable(false);
        dialog.show();

        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            Intent intent = new Intent(this, AcceptedJobsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, 500); // 0.5 seconds as requested
    }
}