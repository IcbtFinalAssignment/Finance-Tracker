package com.icbtcampus.budgettracker_assignment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, SPLASH_DISPLAY_LENGTH);
        // Check for internet connection
        if (!NetworkUtil.isConnected(this)) {
            // If no internet, redirect to NoInternetActivity
            Intent intent = new Intent(SplashActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();  // Close the current activity
        }
    }
}
