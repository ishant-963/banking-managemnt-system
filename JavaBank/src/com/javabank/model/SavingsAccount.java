package com.javabank.model;

import java.sql.Timestamp;

public class SavingsAccount extends Account {
    public static final double MIN_BALANCE = 500.0;
    public static final double INTEREST_RATE = 4.0; // percent per year

    public SavingsAccount(int accNo, int customerId, double balance, AccountStatus status, Timestamp openedOn) {
        super(accNo, customerId, balance, status, openedOn);
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVINGS;
    }

    // a savings account must always keep the minimum balance
    @Override
    public boolean canWithdraw(double amount) {
        return balance - amount >= MIN_BALANCE;
    }

    @Override
    public double calculateMonthlyInterest() {
        double interest = balance * INTEREST_RATE / 100 / 12;
        return Math.round(interest * 100.0) / 100.0;
    }

    @Override
    public String getRulesSummary() {
        return "Savings rules: minimum balance Rs. " + (int) MIN_BALANCE
                + ", interest " + INTEREST_RATE + "% per year.";
    }
}
