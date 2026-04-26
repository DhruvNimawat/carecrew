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
    private TextView tvAfterHint;
    private DatabaseReference mDatabase;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri photoURI;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_completion);

        ticketId = getIntent().getStringExtra("ticketId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints").child(ticketId);

        ivBefore = findViewById(R.id.ivBefore);
        ivAfter = findViewById(R.id.ivAfter);
        tvAfterHint = findViewById(R.id.tvAfterHint);

        loadImages();

        findViewById(R.id.btnCaptureAfter).setOnClickListener(v -> dispatchTakePictureIntent());
        findViewById(R.id.btnUploadContinue).setOnClickListener(v -> finishJob());
    }

    private void loadImages() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Complaint ticket = snapshot.getValue(Complaint.class);
                if (ticket != null && ticket.startWorkImageUrl != null) {
                    Glide.with(JobCompletionActivity.this)
                            .load(ticket.startWorkImageUrl)
                            .into(ivBefore);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void dispatchTakePictureIntent() {
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
            uploadAfterImage();
        }
    }

    private void uploadAfterImage() {
        if (photoURI == null) return;
        
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("work_images/" + ticketId + "_after.jpg");
        storageRef.putFile(photoURI).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                mDatabase.child("afterRepairImageUrl").setValue(uri.toString());
                Glide.with(this).load(uri).into(ivAfter);
                ivAfter.setImageTintList(null); // Remove blue tint when image is loaded
                tvAfterHint.setVisibility(View.GONE);
                Toast.makeText(this, getString(R.string.toast_after_image_saved), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void finishJob() {
        String notes = ((android.widget.EditText)findViewById(R.id.etRepairNotes)).getText().toString().trim();
        if (notes.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_please_enter_notes), Toast.LENGTH_SHORT).show();
            return;
        }

        long endTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String closedAt = sdf.format(new Date(endTime));

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("status", "Completed");
        updates.put("repairNotes", notes);
        updates.put("closedAt", closedAt);
        updates.put("completionTimeMillis", endTime);
        updates.put("staffName", com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0]);

        mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
            showCompletionDialog();
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