package com.icbtcampus.budgettracker_assignment.Activity;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.icbtcampus.budgettracker_assignment.Fragment.FragBudget;
import com.icbtcampus.budgettracker_assignment.Fragment.FragExpense;
import com.icbtcampus.budgettracker_assignment.Fragment.FragIncome;
import com.icbtcampus.budgettracker_assignment.Fragment.FragHistory;
import com.icbtcampus.budgettracker_assignment.Handler.SessionHandler;
import com.icbtcampus.budgettracker_assignment.Model.User;
import com.icbtcampus.budgettracker_assignment.R;
import com.icbtcampus.budgettracker_assignment.Utils.NetworkUtil;

public class DashboardActivity extends AppCompatActivity {
    private SessionHandler session;
    TextView txtTitle;
    ImageView ivAdd,ivSetting;
    private ViewPager viewPager;
    private TabLayout mTabLayout;

    public  String isFrom="INCOME";
    FrameLayout frameBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        session = new SessionHandler(getApplicationContext());

        User user = session.getUserDetails();

        ImageView settingBtn = findViewById(R.id.ivSetting);
        settingBtn.setOnClickListener(view -> {
            Intent intent=new Intent(DashboardActivity.this, SettingActivity.class);
            startActivity(intent);
        });


        // Check for internet connection
        if (!NetworkUtil.isConnected(this)) {
            // If no internet, redirect to NoInternetActivity
            Intent intent = new Intent(DashboardActivity.this, NoInternetActivity.class);
            startActivity(intent);
            finish();  // Close the current activitya
        }
        init();

        setUpFragment();
        // Set initial state for the default tab (Income)
        isFrom = "INCOME";
        ivAdd.setVisibility(View.VISIBLE);
        ivAdd.setOnClickListener(v -> {
            Intent incomeIntent = new Intent(DashboardActivity.this, AddIncomeActivity.class);
            startActivity(incomeIntent);
        });

    }

    private void setUpFragment() {

        viewPager = findViewById(R.id.viewpager_main);
        mTabLayout = findViewById(R.id.tabs_main);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                Log.e("Position--)",""+tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        mTabLayout.addTab(mTabLayout.newTab().setText("Income"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Expense"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Budget"));
        mTabLayout.addTab(mTabLayout.newTab().setText("History"));

        viewPager.setAdapter(new TabPagerAdapter2(getSupportFragmentManager(), mTabLayout.getTabCount()));
        viewPager.setCurrentItem(0);

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: // Income tab selected
                        ivAdd.setVisibility(View.VISIBLE);
                        isFrom = "INCOME";
                        txtTitle.setText("Income");
                        ivAdd.setOnClickListener(v -> {
                            Intent incomeIntent = new Intent(DashboardActivity.this, AddIncomeActivity.class);
                            startActivity(incomeIntent);
                        });
                        break;

                    case 1: // Expense tab selected
                        ivAdd.setVisibility(View.VISIBLE);
                        isFrom = "EXPENSE";
                        txtTitle.setText("Expense");
                        ivAdd.setOnClickListener(v -> {
                            Intent expenseIntent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
                            startActivity(expenseIntent);
                        });
                        break;

                    case 2: // Budget tab selected
                        ivAdd.setVisibility(View.VISIBLE); // Show button for budget
                        txtTitle.setText("Budget");
                        ivAdd.setOnClickListener(v -> {
                            Intent budgetIntent = new Intent(DashboardActivity.this, SetBudgetActivity.class);
                            startActivity(budgetIntent);
                        });
                        break;

                    case 3: // Transactions tab selected
                        ivAdd.setVisibility(View.GONE); // Hide button for transactions
                        txtTitle.setText("History");
                        ivAdd.setOnClickListener(null); // No action for Transactions
                        break;
                }
                Log.e("PositionView-->", "" + position);
            }


            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

                System.out.println("onPageScrolled");
            }

            @Override
            public void onPageScrollStateChanged(int num) {
                // TODO Auto-generated method stub


            }
        });
    }

    public void init()
    {
        txtTitle=findViewById(R.id.txtTitle);
        ivAdd=findViewById(R.id.ivAdd);
        ivSetting=findViewById(R.id.ivSetting);
        frameBanner=findViewById(R.id.frameBanner);



    }


    class TabPagerAdapter2 extends FragmentStatePagerAdapter {

        String which;
        private String[] tabTitles = new String[]{"Income","Expense","Budget","History"};

        public TabPagerAdapter2(FragmentManager fragmentManager, int i) {
            super(fragmentManager);
        }
        @NonNull
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new FragIncome();
                case 1:
                    return new FragExpense();
                case 2:
                    return new FragBudget();
                case 3:
                    return new FragHistory();

                default:
                    return new FragIncome();
            }
        }

        public int getCount() {
            return tabTitles.length;
        }

    }



}
