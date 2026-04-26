package com.example.carecrew;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class UploadImageActivity extends AppCompatActivity {

    private String category, priority, description;
    private ImageView ivPreview;
    private Uri selectedImageUri;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        // Retrieve data from previous step
        category = getIntent().getStringExtra("category");
        priority = getIntent().getStringExtra("priority");
        description = getIntent().getStringExtra("description");

        ivPreview = findViewById(R.id.ivPreview);
        MaterialButton btnCapture = findViewById(R.id.btnCapture);
        MaterialButton btnContinue = findViewById(R.id.btnContinue);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Image Picker Launcher
        ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        updatePreview(uri);
                    }
                }
        );

        // Camera Launcher
        ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        selectedImageUri = cameraImageUri;
                        updatePreview(cameraImageUri);
                    }
                }
        );

        btnCapture.setOnClickListener(v -> showImageOptionDialog(pickImageLauncher, takePhotoLauncher));

        btnContinue.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(UploadImageActivity.this, LocationSelectionActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("priority", priority);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", selectedImageUri.toString());
            startActivity(intent);
        });
    }

    private void showImageOptionDialog(ActivityResultLauncher<String> galleryLauncher, ActivityResultLauncher<Uri> cameraLauncher) {
        String[] options = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Camera
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
                cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                cameraLauncher.launch(cameraImageUri);
            } else {
                // Gallery
                galleryLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void updatePreview(Uri uri) {
        ivPreview.setImageURI(uri);
        ivPreview.setPadding(0, 0, 0, 0);
        ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivPreview.setColorFilter(null);
    }
}