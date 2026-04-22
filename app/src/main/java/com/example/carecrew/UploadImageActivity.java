package com.example.carecrew;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class UploadImageActivity extends AppCompatActivity {

    private String category, priority, description;
    private ImageView ivPreview;
    private Uri selectedImageUri;

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
                        ivPreview.setImageURI(uri);
                        ivPreview.setPadding(0, 0, 0, 0);
                        ivPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ivPreview.setColorFilter(null); // Remove the tint if any
                    }
                }
        );

        btnCapture.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

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
}