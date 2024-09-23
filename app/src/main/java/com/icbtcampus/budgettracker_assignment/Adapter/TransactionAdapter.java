package com.icbtcampus.budgettracker_assignment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.icbtcampus.budgettracker_assignment.Model.Transaction;
import com.icbtcampus.budgettracker_assignment.R;

import java.util.List;
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.txtCategory.setText(transaction.getCategoryName());
        holder.txtDescription.setText(transaction.getDescription());
        holder.txtAmount.setText(String.format("Rs.%,.2f", transaction.getAmount()));
        holder.txtTime.setText(transaction.getFormattedDate()); // Use the formatted date method

        // Set the text color based on transaction type
        if ("income".equals(transaction.getTransactionType())) {
            holder.txtAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.amountColor));
        } else if ("expense".equals(transaction.getTransactionType())) {
            holder.txtAmount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.redColor));
        }
    }


    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory, txtDescription, txtAmount, txtTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
