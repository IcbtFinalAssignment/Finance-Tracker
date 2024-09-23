package com.icbtcampus.budgettracker_assignment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

public class NoInternetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        Button retryButton = findViewById(R.id.retryButton);

        // Retry button to check again for internet and restart from SplashActivity
        retryButton.setOnClickListener(v -> {
            if (NetworkUtil.isConnected(NoInternetActivity.this)) {
                // If connected, navigate to SplashActivity
                Intent intent = new Intent(NoInternetActivity.this, SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears the activity stack
                startActivity(intent);
                finish();  // Close NoInternetActivity
            } else {

                 Toast.makeText(NoInternetActivity.this, "Still no internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
