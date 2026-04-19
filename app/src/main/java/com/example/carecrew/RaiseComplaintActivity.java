package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;

public class RaiseComplaintActivity extends AppCompatActivity {

    private AutoCompleteTextView categoryDropdown, priorityDropdown;
    private TextInputEditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raise_complaint);

        // Setup Category Dropdown
        String[] categories = {"Plumbing", "Electrical", "Carpentry", "Housekeeping", "Other"};
        categoryDropdown = findViewById(R.id.categoryDropdown);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        categoryDropdown.setAdapter(categoryAdapter);

        // Setup Priority Dropdown
        String[] priorities = {"Low", "Medium", "High", "Urgent"};
        priorityDropdown = findViewById(R.id.priorityDropdown);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, priorities);
        priorityDropdown.setAdapter(priorityAdapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            Intent intent = new Intent(RaiseComplaintActivity.this, UploadImageActivity.class);
            intent.putExtra("category", categoryDropdown.getText().toString());
            intent.putExtra("priority", priorityDropdown.getText().toString());
            intent.putExtra("description", descriptionEditText.getText().toString());
            startActivity(intent);
        });
    }
}