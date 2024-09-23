package com.icbtcampus.budgettracker_assignment.Model;

public class Account {
    private int accountId;
    private String accountName;
    private String accountType;

    public Account(int accountId, String accountName, String accountType) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountType = accountType;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    @Override
    public String toString() {
        return accountName + " - " + accountType; // This is what will be displayed in the Spinner
    }
}
