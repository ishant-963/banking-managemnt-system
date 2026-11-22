package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.LoanDAO;
import com.javabank.exception.AccountNotFoundException;
import com.javabank.exception.BankException;
import com.javabank.exception.DatabaseException;
import com.javabank.exception.InvalidAmountException;
import com.javabank.model.Account;
import com.javabank.model.Loan;
import com.javabank.model.LoanStatus;
import com.javabank.model.TransactionType;
import com.javabank.util.DBConnection;
import com.javabank.util.MoneyFormatter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class LoanService {

    public static final double ANNUAL_RATE = 10.5;
    public static final double MIN_LOAN = 5000.0;
    public static final int MIN_MONTHS = 6;
    public static final int MAX_MONTHS = 60;
    public static final int MAX_ACTIVE_LOANS = 2;
    public static final int LOAN_LIMIT_MULTIPLE = 10; // can borrow up to 10x the account balance

    private AccountDAO accountDAO = new AccountDAO();
    private LoanDAO loanDAO = new LoanDAO();
    private TransactionService transactionService = new TransactionService();

    // standard EMI formula: P * r * (1+r)^n / ((1+r)^n - 1)
    public double calculateEmi(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 12 / 100;
        double growth = Math.pow(1 + monthlyRate, months);
        double emi = principal * monthlyRate * growth / (growth - 1);
        return MoneyFormatter.round(emi);
    }

    public double getMaxLoanFor(Account account) {
        return Math.max(0, MoneyFormatter.round(account.getBalance() * LOAN_LIMIT_MULTIPLE));
    }

    public Loan applyForLoan(int accNo, double principal, int months) throws BankException {
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw new AccountNotFoundException(accNo);
        }
        if (!account.isActive()) {
            throw new BankException("Loans can only be given to active accounts.");
        }
        if (principal < MIN_LOAN) {
            throw new InvalidAmountException("Minimum loan amount is " + MoneyFormatter.format(MIN_LOAN) + ".");
        }
        if (months < MIN_MONTHS || months > MAX_MONTHS) {
            throw new BankException("Loan period must be between " + MIN_MONTHS + " and " + MAX_MONTHS + " months.");
        }
        if (principal > getMaxLoanFor(account)) {
            throw new BankException("Based on your balance you can borrow up to "
                    + MoneyFormatter.format(getMaxLoanFor(account)) + ".");
        }
        if (countActiveLoans(account.getCustomerId()) >= MAX_ACTIVE_LOANS) {
            throw new BankException("You already have " + MAX_ACTIVE_LOANS + " active loans. Please close one first.");
        }

        double emi = calculateEmi(principal, ANNUAL_RATE, months);
        Loan loan = new Loan(account.getCustomerId(), accNo, principal, ANNUAL_RATE, months, emi);

        Connection con = null;
        int loanId = -1;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            loanId = loanDAO.insert(con, loan);
            transactionService.postWithin(con, account, principal, TransactionType.LOAN_DISBURSAL,
                    "Loan #" + loanId + " disbursed");

            con.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Could not process the loan: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
        return loanDAO.findById(loanId);
    }

    // pays one EMI (or the last smaller amount) from the given account
    public Loan payEmi(int loanId, int accNo) throws BankException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new BankException("Loan " + loanId + " was not found.");
        }
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw new AccountNotFoundException(accNo);
        }
        if (loan.getCustomerId() != account.getCustomerId()) {
            throw new BankException("This loan does not belong to you.");
        }
        if (loan.getStatus() == LoanStatus.CLOSED) {
            throw new BankException("Loan " + loanId + " is already fully paid.");
        }

        double payment = Math.min(loan.getEmi(), loan.getRemaining());
        double newRemaining = MoneyFormatter.round(loan.getRemaining() - payment);
        LoanStatus newStatus = newRemaining <= 0.01 ? LoanStatus.CLOSED : LoanStatus.ACTIVE;
        if (newStatus == LoanStatus.CLOSED) {
            newRemaining = 0;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            transactionService.postWithin(con, account, -payment, TransactionType.LOAN_EMI,
                    "EMI for loan #" + loanId);
            loanDAO.updateRemaining(con, loanId, newRemaining, newStatus);

            con.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Could not pay the EMI: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
        return loanDAO.findById(loanId);
    }

    public ArrayList<Loan> getLoansOfCustomer(int customerId) throws BankException {
        return loanDAO.findByCustomer(customerId);
    }

    public ArrayList<Loan> getAllLoans() throws BankException {
        return loanDAO.findAll();
    }

    private int countActiveLoans(int customerId) throws BankException {
        int count = 0;
        for (Loan loan : loanDAO.findByCustomer(customerId)) {
            if (loan.getStatus() == LoanStatus.ACTIVE) {
                count++;
            }
        }
        return count;
    }
}
