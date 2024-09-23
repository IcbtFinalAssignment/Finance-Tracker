package com.icbtcampus.budgettracker_assignment.Activity;




import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.Model.User;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.ApiKeyUtil;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;


public class SettingActivity extends AppCompatActivity {


    private SessionHandler session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();
        //full name
        TextView fullName = findViewById(R.id.FullName);
        fullName.setText(user.getFullName());
        //email
        TextView email = findViewById(R.id.Email);
        email.setText(user.getEmail());

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> finish());

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutConfirmationDialog();
            }
        });

        // Check for internet connection
        if (!NetworkUtil.isConnected(this)) {
            // If no internet, redirect to NoInternetActivity
            Intent intent = new Intent(SettingActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();  // Close the current activity
        }
        LinearLayout myLinearLayout = findViewById(R.id.changePwd);
        myLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });



    }
    private void LogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed logout
                        session.logoutUser();
                        Intent i = new Intent(SettingActivity.this, LoginActivity.class);
                        startActivity(i);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User cancelled logout
                        dialog.dismiss();
                    }
                })
                .show();
    }
}