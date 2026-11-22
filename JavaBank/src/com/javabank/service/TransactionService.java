package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.AccountFrozenException;
import com.javabank.exception.AccountNotFoundException;
import com.javabank.exception.BankException;
import com.javabank.exception.DatabaseException;
import com.javabank.exception.InsufficientFundsException;
import com.javabank.exception.InvalidAmountException;
import com.javabank.model.Account;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.DBConnection;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class TransactionService {

    public static final double MAX_DEPOSIT = 1000000.0;
    public static final double MAX_WITHDRAWAL = 50000.0;

    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    public Account deposit(int accNo, double amount) throws BankException {
        checkAmount(amount);
        if (amount > MAX_DEPOSIT) {
            throw new InvalidAmountException("Single deposit limit is " + MoneyFormatter.format(MAX_DEPOSIT) + ".");
        }
        return postSingle(accNo, amount, TransactionType.DEPOSIT, "Cash deposit");
    }

    public Account withdraw(int accNo, double amount) throws BankException {
        checkAmount(amount);
        if (amount > MAX_WITHDRAWAL) {
            throw new InvalidAmountException("Single withdrawal limit is " + MoneyFormatter.format(MAX_WITHDRAWAL) + ".");
        }
        return postSingle(accNo, -amount, TransactionType.WITHDRAWAL, "Cash withdrawal");
    }

    // one entry in its own database transaction (positive = money in, negative = money out)
    public Account postSingle(int accNo, double signedAmount, TransactionType type, String description)
            throws BankException {
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw new AccountNotFoundException(accNo);
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            postWithin(con, account, signedAmount, type, description);

            con.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Transaction failed: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
        return accountDAO.findByAccNo(accNo);
    }

    // The core rule check + the two writes. It never commits, so transfers and loans
    // can call it several times inside one bigger database transaction.
    public void postWithin(Connection con, Account account, double signedAmount,
                           TransactionType type, String description) throws BankException {
        double amount = MoneyFormatter.round(Math.abs(signedAmount));

        if (!account.isActive()) {
            throw new AccountFrozenException(account.getAccNo());
        }
        if (signedAmount < 0 && !account.canWithdraw(amount)) {
            throw new InsufficientFundsException(account, amount);
        }

        double newBalance = MoneyFormatter.round(account.getBalance() + signedAmount);
        accountDAO.updateBalance(con, account.getAccNo(), newBalance);
        transactionDAO.insert(con, new Transaction(account.getAccNo(), type, amount, newBalance, description));
    }

    public ArrayList<Transaction> getMiniStatement(int accNo) throws BankException {
        return transactionDAO.findLatestByAccount(accNo, 5);
    }

    public ArrayList<Transaction> getRecentTransactions(int limit) throws BankException {
        if (limit < 1 || limit > 200) {
            throw new BankException("Please ask for between 1 and 200 transactions.");
        }
        return transactionDAO.findRecent(limit);
    }

    private void checkAmount(double amount) throws InvalidAmountException {
        if (!Validator.isValidAmount(amount)) {
            throw new InvalidAmountException("Amount must be at least Rs. 0.01.");
        }
    }
}
