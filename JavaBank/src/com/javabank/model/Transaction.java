package com.javabank.model;

import java.sql.Timestamp;

public class Transaction {
    private int txnId;
    private int accNo;
    private TransactionType type;
    private double amount;
    private double balanceAfter;
    private String description;
    private Timestamp time;

    // new transaction that is about to be saved
    public Transaction(int accNo, TransactionType type, double amount, double balanceAfter, String description) {
        this.accNo = accNo;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    // transaction read from the database
    public Transaction(int txnId, int accNo, TransactionType type, double amount,
                       double balanceAfter, String description, Timestamp time) {
        this(accNo, type, amount, balanceAfter, description);
        this.txnId = txnId;
        this.time = time;
    }

    public int getTxnId() {
        return txnId;
    }

    public int getAccNo() {
        return accNo;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getTime() {
        return time;
    }
}
