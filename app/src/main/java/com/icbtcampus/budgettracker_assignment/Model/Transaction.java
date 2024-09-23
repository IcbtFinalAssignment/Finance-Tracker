package com.icbtcampus.budgettracker_assignment.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    private int transactionId;
    private int categoryId;
    private double amount;
    private String transactionType;
    private int accountId;
    private String description;
    private String transactionDate;
    private String categoryName;

    // Updated Constructor
    public Transaction(int transactionId, int categoryId, double amount, String transactionType, int accountId, String description, String transactionDate, String categoryName) {
        this.transactionId = transactionId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.accountId = accountId;
        this.description = description;
        this.transactionDate = transactionDate;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    // Method to get formatted date and time
    public String getFormattedDate() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        String formattedDate = "";

        try {
            Date date = inputFormat.parse(transactionDate);
            formattedDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }
}
