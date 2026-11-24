package com.javabank.model;

import java.sql.Timestamp;

// Represents a bank account (SAVINGS or CURRENT) and its type-specific rules.
public class Account {

    public static final double SAVINGS_MIN_BALANCE = 500.0;
    public static final double SAVINGS_INTEREST_RATE = 4.0;
    public static final double CURRENT_OVERDRAFT_LIMIT = 10000.0;
    public static final double CURRENT_MIN_OPENING_DEPOSIT = 1000.0;

    private int accNo;
    private int customerId;
    private AccountType type;
    private double balance;
    private AccountStatus status;
    private Timestamp openedOn;

    public Account(int accNo, int customerId, AccountType type, double balance, AccountStatus status, Timestamp openedOn) {
        this.accNo = accNo;
        this.customerId = customerId;
        this.type = type;
        this.balance = balance;
        this.status = status;
        this.openedOn = openedOn;
    }

    public AccountType getType() {
        return type;
    }

    public boolean canWithdraw(double amount) {
        if (type == AccountType.SAVINGS) {
            return balance - amount >= SAVINGS_MIN_BALANCE;
        }
        return balance - amount >= -CURRENT_OVERDRAFT_LIMIT;
    }

    public double calculateMonthlyInterest() {
        if (type != AccountType.SAVINGS) {
            return 0.0;
        }
        double interest = balance * SAVINGS_INTEREST_RATE / 100 / 12;
        return Math.round(interest * 100.0) / 100.0;
    }

    public String getRulesSummary() {
        if (type == AccountType.SAVINGS) {
            return "Savings rules: minimum balance Rs. " + (int) SAVINGS_MIN_BALANCE
                    + ", interest " + SAVINGS_INTEREST_RATE + "% per year.";
        }
        return "Current rules: overdraft up to Rs. " + (int) CURRENT_OVERDRAFT_LIMIT + ", no interest.";
    }

    public static double getMinimumOpeningDeposit(AccountType type) {
        if (type == AccountType.SAVINGS) {
            return SAVINGS_MIN_BALANCE;
        }
        return CURRENT_MIN_OPENING_DEPOSIT;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public int getAccNo() {
        return accNo;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Timestamp getOpenedOn() {
        return openedOn;
    }
}
