package com.example.carecrew;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        TextView tvUid = findViewById(R.id.tvProfileUid);

        if (user != null) {
            tvEmail.setText(user.getEmail());
            tvUid.setText(user.getUid());
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}