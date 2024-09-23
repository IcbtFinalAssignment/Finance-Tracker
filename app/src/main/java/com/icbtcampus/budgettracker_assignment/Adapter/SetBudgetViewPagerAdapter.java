package com.icbtcampus.budgettracker_assignment.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.icbtcampus.budgettracker_assignment.Fragment.BudgetIncomeFragment;
import com.icbtcampus.budgettracker_assignment.Fragment.BudgetExpenseFragment;

public class SetBudgetViewPagerAdapter extends FragmentStateAdapter {

    public SetBudgetViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BudgetIncomeFragment(); // Income tab
            case 1:
                return new BudgetExpenseFragment(); // Expense tab
            default:
                return new BudgetIncomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs: Income and Expense
    }
}
