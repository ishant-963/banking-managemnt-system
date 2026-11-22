package com.javabank.model;

import java.sql.Timestamp;

public class Loan {
    private int loanId;
    private int customerId;
    private int accNo;
    private double principal;
    private double annualRate;
    private int months;
    private double emi;
    private double remaining;
    private LoanStatus status;
    private Timestamp takenOn;

    // new loan: the customer owes emi * months in total
    public Loan(int customerId, int accNo, double principal, double annualRate, int months, double emi) {
        this.customerId = customerId;
        this.accNo = accNo;
        this.principal = principal;
        this.annualRate = annualRate;
        this.months = months;
        this.emi = emi;
        this.remaining = Math.round(emi * months * 100.0) / 100.0;
        this.status = LoanStatus.ACTIVE;
    }

    // loan read from the database
    public Loan(int loanId, int customerId, int accNo, double principal, double annualRate, int months,
                double emi, double remaining, LoanStatus status, Timestamp takenOn) {
        this.loanId = loanId;
        this.customerId = customerId;
        this.accNo = accNo;
        this.principal = principal;
        this.annualRate = annualRate;
        this.months = months;
        this.emi = emi;
        this.remaining = remaining;
        this.status = status;
        this.takenOn = takenOn;
    }

    public int getLoanId() {
        return loanId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getAccNo() {
        return accNo;
    }

    public double getPrincipal() {
        return principal;
    }

    public double getAnnualRate() {
        return annualRate;
    }

    public int getMonths() {
        return months;
    }

    public double getEmi() {
        return emi;
    }

    public double getRemaining() {
        return remaining;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public Timestamp getTakenOn() {
        return takenOn;
    }
}
