package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UploadImageActivity extends AppCompatActivity {

    private String category, priority, description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        category = getIntent().getStringExtra("category");
        priority = getIntent().getStringExtra("priority");
        description = getIntent().getStringExtra("description");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnCapture).setOnClickListener(v -> {
            Toast.makeText(this, "Opening selector...", Toast.LENGTH_SHORT).show();
            // Mock image selection
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Intent intent = new Intent(UploadImageActivity.this, LocationSelectionActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("priority", priority);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", "default_image_url"); 
            startActivity(intent);
        });
    }
}