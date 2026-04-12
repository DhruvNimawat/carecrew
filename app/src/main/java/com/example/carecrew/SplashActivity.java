package com.example.carecrew;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // Move to MainActivity
                Intent intent = new Intent(SplashActivity.this, RoleSelectionActivity.class);
                startActivity(intent);

                finish(); // close splash so user can't come back
            }
        }, SPLASH_TIME);
    }
}