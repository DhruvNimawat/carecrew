package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

public class LocationSelectionActivity extends AppCompatActivity {

    private String category, priority, description, imageUrl;
    private AutoCompleteTextView blockDropdown, floorDropdown;
    private TextInputEditText roomEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        category = getIntent().getStringExtra("category");
        priority = getIntent().getStringExtra("priority");
        description = getIntent().getStringExtra("description");
        imageUrl = getIntent().getStringExtra("imageUrl");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Block Dropdown
        String[] blocks = getResources().getStringArray(R.array.buildings_array);
        blockDropdown = findViewById(R.id.blockDropdown);
        ArrayAdapter<String> blockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blocks);
        blockDropdown.setAdapter(blockAdapter);

        // Setup Floor Dropdown
        String[] floors = getResources().getStringArray(R.array.floors_array);
        floorDropdown = findViewById(R.id.floorDropdown);
        ArrayAdapter<String> floorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, floors);
        floorDropdown.setAdapter(floorAdapter);

        roomEditText = findViewById(R.id.roomEditText);

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Intent intent = new Intent(LocationSelectionActivity.this, ComplaintReviewActivity.class);
            intent.putExtra("category", category);
            intent.putExtra("priority", priority);
            intent.putExtra("description", description);
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("block", blockDropdown.getText().toString());
            intent.putExtra("floor", floorDropdown.getText().toString());
            intent.putExtra("roomNumber", roomEditText.getText().toString());
            startActivity(intent);
        });
    }
}