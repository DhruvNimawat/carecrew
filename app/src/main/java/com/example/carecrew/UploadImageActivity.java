package com.example.carecrew;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class UploadImageActivity extends AppCompatActivity {

    private String category, priority, description;
    private ImageView ivPreview;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    ivPreview.setImageURI(imageUri);
                    ivPreview.setPadding(0, 0, 0, 0);
                    ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivPreview.setImageURI(imageUri);
                    ivPreview.setPadding(0, 0, 0, 0);
                    ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        category = getIntent().getStringExtra("category");
        priority = getIntent().getStringExtra("priority");
        description = getIntent().getStringExtra("description");

        ivPreview = findViewById(R.id.ivPreview);
        ivPreview.setOnClickListener(v -> {
            if (imageUri != null) {
                showFullScreenPreview();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        findViewById(R.id.btnGallery).setOnClickListener(v -> galleryLauncher.launch("image/*"));

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Intent intent = new Intent(UploadImageActivity.this, LocationSelectionActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("priority", priority);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", imageUri != null ? imageUri.toString() : ""); 
            startActivity(intent);
        });
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(intent);
    }

    private void showFullScreenPreview() {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_full_screen_image);
        
        ImageView fullScreenImage = dialog.findViewById(R.id.fullScreenImageView);
        ImageView btnClose = dialog.findViewById(R.id.btnClosePreview);
        
        fullScreenImage.setImageURI(imageUri);
        
        btnClose.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
}