package com.icbtcampus.budgettracker_assignment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.icbtcampus.budgettracker_assignment.Model.Category;
import com.icbtcampus.budgettracker_assignment.R;

import java.text.NumberFormat;
import java.util.List;

public class BudgetFragCategoriesAdapter extends RecyclerView.Adapter<BudgetFragCategoriesAdapter.ViewHolder> {

    private List<Category> categories;

    public BudgetFragCategoriesAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget_fragment_categories, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getCategoryName());

        // Format the budget amount with thousand separators
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        String formattedAmount = numberFormat.format(category.getBudgetAmount());

        // Prepend "Rs." to the formatted amount
        String displayAmount = "Rs." + formattedAmount;
        holder.budgetAmount.setText(displayAmount);

        // Set color based on income or expense
        if (category.isIncome()) {
            holder.budgetBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.amountColor));
        } else {
            holder.budgetBar.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.redColor));
        }

        // Optionally adjust text visibility if needed
        holder.budgetAmount.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView budgetAmount;
        View budgetBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.tvCategoryName);
            budgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            budgetBar = itemView.findViewById(R.id.budgetBar);
        }
    }
}
