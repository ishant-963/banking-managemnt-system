package com.javabank.model;

import java.sql.Timestamp;

// Base class for every kind of account. Savings and current accounts
// differ in their withdrawal rules and in how they earn interest.
public abstract class Account {
    protected int accNo;
    protected int customerId;
    protected double balance;
    protected AccountStatus status;
    protected Timestamp openedOn;

    public Account(int accNo, int customerId, double balance, AccountStatus status, Timestamp openedOn) {
        this.accNo = accNo;
        this.customerId = customerId;
        this.balance = balance;
        this.status = status;
        this.openedOn = openedOn;
    }

    public abstract AccountType getType();

    public abstract boolean canWithdraw(double amount);

    public abstract double calculateMonthlyInterest();

    public abstract String getRulesSummary();

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
