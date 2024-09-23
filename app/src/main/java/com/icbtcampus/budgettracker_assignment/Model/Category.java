package com.icbtcampus.budgettracker_assignment.Model;
public class Category {
    private int categoryId;
    private String categoryName;
    private double budgetAmount;
    private boolean isIncome; // New field

    public Category(int categoryId, String categoryName, double budgetAmount, boolean isIncome) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.isIncome = isIncome;
    }

    // Getters and setters
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public double getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(double budgetAmount) { this.budgetAmount = budgetAmount; }

    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean isIncome) { this.isIncome = isIncome; }
}
