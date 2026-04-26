package com.example.carecrew;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout contentFrame;
    private CardView sharedLogoCard;
    private ObjectAnimator rotationAnimator;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentFrame = findViewById(R.id.contentFrame);
        sharedLogoCard = findViewById(R.id.sharedLogoCard);

        showSplashScreen();
    }

    private void showSplashScreen() {
        View splashView = LayoutInflater.from(this).inflate(R.layout.activity_splash, contentFrame, false);
        contentFrame.removeAllViews();
        contentFrame.addView(splashView);

        // UI elements for splash animation
        View appName = splashView.findViewById(R.id.appName);
        View tagline = splashView.findViewById(R.id.tagline);
        View logoPlaceholder = splashView.findViewById(R.id.logoPlaceholder);

        // Load animations
        Animation topDown = AnimationUtils.loadAnimation(this, R.anim.top_down);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Positioning and rotating the shared logo
        handler.postDelayed(() -> {
            int[] location = new int[2];
            logoPlaceholder.getLocationOnScreen(location);
            sharedLogoCard.setX(location[0]);
            sharedLogoCard.setY(location[1]);
            sharedLogoCard.setVisibility(View.VISIBLE);

            // Infinite rotation using ObjectAnimator
            rotationAnimator = ObjectAnimator.ofFloat(sharedLogoCard, "rotation", 0f, 360f);
            rotationAnimator.setDuration(2000);
            rotationAnimator.setInterpolator(new LinearInterpolator());
            rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            rotationAnimator.start();
        }, 100);

        appName.startAnimation(topDown);
        tagline.startAnimation(fadeIn);

        // Transition to Role Selection after 3 seconds
        handler.postDelayed(this::showRoleSelection, 3000);
    }

    private void showRoleSelection() {
        View roleView = LayoutInflater.from(this).inflate(R.layout.activity_role_selection, contentFrame, false);
        
        // Preparation: Find target position in the new layout
        View logoPlaceholder = roleView.findViewById(R.id.logoPlaceholder);
        View headerTextSection = roleView.findViewById(R.id.headerTextSection);
        View topHeaderBg = roleView.findViewById(R.id.topHeaderBg);
        View headerCurve = roleView.findViewById(R.id.headerCurve);

        // Clear and add new view
        contentFrame.removeAllViews();
        contentFrame.addView(roleView);

        // Stop rotation and animate shared logo to new position
        if (rotationAnimator != null) {
            rotationAnimator.cancel();
            sharedLogoCard.setRotation(0f); // Reset to fixed position
        }

        handler.post(() -> {
            int[] targetLocation = new int[2];
            logoPlaceholder.getLocationOnScreen(targetLocation);

            sharedLogoCard.animate()
                    .x(targetLocation[0])
                    .y(targetLocation[1])
                    .scaleX(0.83f) // 100dp / 120dp approx
                    .scaleY(0.83f)
                    .alpha(1.0f)
                    .setDuration(800)
                    .setInterpolator(new android.view.animation.PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f)) // Standard easing
                    .start();
        });

        // Animate other elements with a small delay for smoothness
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation topDown = AnimationUtils.loadAnimation(this, R.anim.top_down);
        Animation popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in);

        topHeaderBg.setVisibility(View.INVISIBLE);
        headerCurve.setVisibility(View.INVISIBLE);
        headerTextSection.setVisibility(View.INVISIBLE);

        handler.postDelayed(() -> {
            topHeaderBg.setVisibility(View.VISIBLE);
            headerCurve.setVisibility(View.VISIBLE);
            headerTextSection.setVisibility(View.VISIBLE);
            topHeaderBg.startAnimation(topDown);
            headerCurve.startAnimation(topDown);
            headerTextSection.startAnimation(fadeIn);
        }, 300);

        // Staggered animation for cards using ViewPropertyAnimator for much smoother transitions
        CardView cardStudent = roleView.findViewById(R.id.cardStudent);
        CardView cardAdmin = roleView.findViewById(R.id.cardAdmin);
        CardView cardStaff = roleView.findViewById(R.id.cardStaff);
        CardView cardWarden = roleView.findViewById(R.id.cardWarden);

        View[] cards = {cardStudent, cardAdmin, cardStaff, cardWarden};
        for (int i = 0; i < cards.length; i++) {
            final View card = cards[i];
            
            // Set initial hidden state
            card.setAlpha(0f);
            card.setTranslationY(80f);
            card.setScaleX(0.92f);
            card.setScaleY(0.92f);
            
            // Animate each card with a staggered delay and smooth overshoot
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(700)
                    .setStartDelay(900 + (i * 120)) // Start after logo animation is mostly done
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.1f))
                    .start();
        }

        setupClickListeners(roleView);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.cardStudent).setOnClickListener(v -> onRoleSelected("Student"));
        view.findViewById(R.id.cardAdmin).setOnClickListener(v -> onRoleSelected("Admin"));
        view.findViewById(R.id.cardStaff).setOnClickListener(v -> onRoleSelected("Staff"));
        view.findViewById(R.id.cardWarden).setOnClickListener(v -> onRoleSelected("Warden"));
    }

    private void onRoleSelected(String role) {
        View card = null;
        Class<?> targetActivity = null;
        switch (role) {
            case "Student":
                card = findViewById(R.id.cardStudent);
                targetActivity = UserLoginActivity.class;
                break;
            case "Admin":
                card = findViewById(R.id.cardAdmin);
                targetActivity = AdminLoginActivity.class;
                break;
            case "Staff":
                card = findViewById(R.id.cardStaff);
                targetActivity = StaffLoginActivity.class;
                break;
            case "Warden":
                card = findViewById(R.id.cardWarden);
                targetActivity = WardenLoginActivity.class;
                break;
        }

        if (card != null) {
            Animation clickShrink = AnimationUtils.loadAnimation(this, R.anim.click_shrink);
            card.startAnimation(clickShrink);
        }

        if (targetActivity != null) {
            final Class<?> finalTarget = targetActivity;
            handler.postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, finalTarget);
                intent.putExtra("ROLE", role);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }, 200);
        }
    }
}