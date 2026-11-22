package com.javabank.model;

import java.sql.Timestamp;

public class CurrentAccount extends Account {
    public static final double OVERDRAFT_LIMIT = 10000.0;
    public static final double MIN_OPENING_DEPOSIT = 1000.0;

    public CurrentAccount(int accNo, int customerId, double balance, AccountStatus status, Timestamp openedOn) {
        super(accNo, customerId, balance, status, openedOn);
    }

    @Override
    public AccountType getType() {
        return AccountType.CURRENT;
    }

    // a current account is allowed to go negative up to the overdraft limit
    @Override
    public boolean canWithdraw(double amount) {
        return balance - amount >= -OVERDRAFT_LIMIT;
    }

    @Override
    public double calculateMonthlyInterest() {
        return 0.0; // current accounts do not earn interest
    }

    @Override
    public String getRulesSummary() {
        return "Current rules: overdraft up to Rs. " + (int) OVERDRAFT_LIMIT + ", no interest.";
    }
}
