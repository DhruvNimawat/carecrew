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
        String[] blocks = {
            "GA Building", "GB Building", "NB", "Bhagat Singh", 
            "Vikram Sarabhai", "Ratan Tata", "APJ Abdul Kalam", 
            "Kalpana Chawla", "Gargi", "Workshop"
        };
        blockDropdown = findViewById(R.id.blockDropdown);
        ArrayAdapter<String> blockAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, blocks);
        blockDropdown.setAdapter(blockAdapter);

        floorDropdown = findViewById(R.id.floorDropdown);

        blockDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBlock = blocks[position];
            updateFloorDropdown(selectedBlock);
        });

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

    private void updateFloorDropdown(String block) {
        java.util.List<String> floors = new java.util.ArrayList<>();
        
        if (block.equals("GA Building") || block.equals("GB Building")) {
            floors.add("LG");
            floors.add("UG");
            for (int i = 1; i <= 3; i++) floors.add(getOrdinal(i) + " Floor");
        } else if (block.equals("NB")) {
            floors.add("Ground Floor");
            for (int i = 1; i <= 4; i++) floors.add(getOrdinal(i) + " Floor");
        } else if (block.equals("Bhagat Singh") || block.equals("Ratan Tata") || block.equals("Kalpana Chawla")) {
            for (int i = 1; i <= 14; i++) floors.add(getOrdinal(i) + " Floor");
        } else if (block.equals("Vikram Sarabhai") || block.equals("Gargi")) {
            for (int i = 1; i <= 10; i++) floors.add(getOrdinal(i) + " Floor");
        } else if (block.equals("APJ Abdul Kalam")) {
            for (int i = 1; i <= 8; i++) floors.add(getOrdinal(i) + " Floor");
        } else if (block.equals("Workshop")) {
            floors.add("Ground Floor");
            floors.add("1st Floor");
        }

        ArrayAdapter<String> floorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, floors);
        floorDropdown.setAdapter(floorAdapter);
        floorDropdown.setText("", false); // Clear previous selection
    }

    private String getOrdinal(int i) {
        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }
}