package com.icbtcampus.budgettracker_assignment.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.icbtcampus.budgettracker_assignment.Model.Category;
import com.icbtcampus.budgettracker_assignment.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SetBudgetCategoriesAdapter extends RecyclerView.Adapter<SetBudgetCategoriesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Category> categories;
    private OnItemClickListener onItemClickListener;

    public SetBudgetCategoriesAdapter(Context context, ArrayList<Category> categories, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.categories = categories;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_set_budget_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getCategoryName());

        // Format budget amount with thousand separators
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        String formattedBudgetAmount = numberFormat.format(category.getBudgetAmount());

        // Add "Rs." before the formatted amount
        holder.budgetAmount.setText("Rs. " + formattedBudgetAmount);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView budgetAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            budgetAmount = itemView.findViewById(R.id.budgetAmount);
        }
    }
}
