package com.example.carecrew;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashMap;
import java.util.Map;

public class CancelJobActivity extends AppCompatActivity {

    private String ticketId;
    private EditText etReason;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_job);

        ticketId = getIntent().getStringExtra("ticketId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("complaints").child(ticketId);

        etReason = findViewById(R.id.etReason);
        findViewById(R.id.btnSubmitCancel).setOnClickListener(v -> submitCancellation());
    }

    private void submitCancellation() {
        String reason = etReason.getText().toString().trim();
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Cancelled");
        updates.put("assignedTo", null); // Return to available pool
        updates.put("cancelReason", reason);
        updates.put("cancelledBy", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        updates.put("completionTimeMillis", System.currentTimeMillis());

        mDatabase.updateChildren(updates).addOnSuccessListener(aVoid -> {
            showCancellationDialog();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to cancel job", Toast.LENGTH_SHORT).show();
        });
    }

    private void showCancellationDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_job_done); 
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        android.widget.TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        if (tvTitle != null) tvTitle.setText("Job Cancelled");
        
        android.widget.TextView tvSubtitle = dialog.findViewById(R.id.tvDialogSubtitle);
        if (tvSubtitle != null) {
            tvSubtitle.setText("Cancelled");
            tvSubtitle.setTextColor(android.graphics.Color.RED);
        }
        
        dialog.setCancelable(false);
        dialog.show();

        new android.os.Handler().postDelayed(() -> {
            dialog.dismiss();
            android.content.Intent intent = new android.content.Intent(this, AcceptedJobsActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }, 500);
    }
}