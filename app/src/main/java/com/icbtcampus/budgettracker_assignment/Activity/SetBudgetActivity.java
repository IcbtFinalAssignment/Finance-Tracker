package com.icbtcampus.budgettracker_assignment.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.icbtcampus.budgettracker_assignment.Adapter.SetBudgetViewPagerAdapter;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

public class SetBudgetActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private SetBudgetViewPagerAdapter setBudgetViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_budget);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        setBudgetViewPagerAdapter = new SetBudgetViewPagerAdapter(this);
        viewPager.setAdapter(setBudgetViewPagerAdapter);
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> onBackPressed());

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Income");
                    break;
                case 1:
                    tab.setText("Expense");
                    break;
            }
        }).attach();
        // Check for internet connection
        if (!NetworkUtil.isConnected(this)) {
            // If no internet, redirect to NoInternetActivity
            Intent intent = new Intent(SetBudgetActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();  // Close the current activity
        }
    }
}
