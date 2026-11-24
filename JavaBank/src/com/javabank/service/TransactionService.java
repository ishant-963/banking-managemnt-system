package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.BankException;
import com.javabank.exception.DatabaseException;
import com.javabank.model.Account;
import com.javabank.model.AccountType;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.DBConnection;
import com.javabank.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class TransactionService {

    public static final double MAX_DEPOSIT = 1000000.0;
    public static final double MAX_WITHDRAWAL = 50000.0;
    public static final double TRANSFER_LIMIT = 200000.0;

    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    public Account deposit(int accNo, double amount) throws BankException {
        checkAmount(amount, MAX_DEPOSIT, "deposit");
        return postSingle(accNo, amount, TransactionType.DEPOSIT, "Cash deposit");
    }

    public Account withdraw(int accNo, double amount) throws BankException {
        checkAmount(amount, MAX_WITHDRAWAL, "withdrawal");
        return postSingle(accNo, -amount, TransactionType.WITHDRAWAL, "Cash withdrawal");
    }

    // Both sides are saved in one database transaction: either both happen or neither does.
    public void transfer(int fromAccNo, int toAccNo, double amount, String note) throws BankException {
        checkAmount(amount, TRANSFER_LIMIT, "transfer");
        if (fromAccNo == toAccNo) {
            throw new BankException("You cannot transfer money to the same account.");
        }

        Account from = requireAccount(fromAccNo);
        Account to = requireAccount(toAccNo);

        String suffix = "";
        if (note != null && !note.isEmpty()) {
            if (note.length() > 40) {
                suffix = " - " + note.substring(0, 40);
            } else {
                suffix = " - " + note;
            }
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            postWithin(con, from, -amount, TransactionType.TRANSFER_OUT, "Transfer to " + toAccNo + suffix);
            postWithin(con, to, amount, TransactionType.TRANSFER_IN, "Transfer from " + fromAccNo + suffix);

            con.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Transfer failed: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
    }

    // Credits one month of interest to every active savings account.
    public ArrayList<String> applyMonthlyInterest() throws BankException {
        ArrayList<String> lines = new ArrayList<>();
        double total = 0;

        for (Account account : accountDAO.findByType(AccountType.SAVINGS)) {
            if (!account.isActive() || transactionDAO.hasInterestThisMonth(account.getAccNo())) {
                continue;
            }
            double interest = account.calculateMonthlyInterest();
            if (interest <= 0) {
                continue;
            }
            try {
                postSingle(account.getAccNo(), interest, TransactionType.INTEREST, "Monthly interest");
                lines.add("  Account " + account.getAccNo() + " -> " + ConsoleUtil.formatMoney(interest));
                total += interest;
            } catch (BankException e) {
                // one bad account should not stop the rest
            }
        }
        if (!lines.isEmpty()) {
            lines.add("TOTAL:" + ConsoleUtil.formatMoney(total));
        }
        return lines;
    }

    // one entry in its own database transaction (positive = money in, negative = money out)
    public Account postSingle(int accNo, double signedAmount, TransactionType type, String description)
            throws BankException {
        Account account = requireAccount(accNo);
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

    // The core rule check + the two writes. Never commits, so transfers and loans share one transaction.
    public void postWithin(Connection con, Account account, double signedAmount,
                           TransactionType type, String description) throws BankException {
        double amount = ConsoleUtil.roundMoney(Math.abs(signedAmount));

        if (!account.isActive()) {
            throw BankException.accountFrozen(account.getAccNo());
        }
        if (signedAmount < 0 && !account.canWithdraw(amount)) {
            throw BankException.insufficientFunds(account, amount);
        }

        double newBalance = ConsoleUtil.roundMoney(account.getBalance() + signedAmount);
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

    private Account requireAccount(int accNo) throws BankException {
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw BankException.accountNotFound(accNo);
        }
        return account;
    }

    private void checkAmount(double amount, double maxLimit, String label) throws BankException {
        if (!Validator.isValidAmount(amount)) {
            throw new BankException("Amount must be at least Rs. 0.01.");
        }
        if (amount > maxLimit) {
            throw new BankException("Single " + label + " limit is " + ConsoleUtil.formatMoney(maxLimit) + ".");
        }
    }
}
