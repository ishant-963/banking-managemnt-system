package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.exception.AccountNotFoundException;
import com.javabank.exception.BankException;
import com.javabank.exception.DatabaseException;
import com.javabank.exception.InvalidAmountException;
import com.javabank.model.Account;
import com.javabank.model.TransactionType;
import com.javabank.util.DBConnection;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;

public class TransferService {

    public static final double TRANSFER_LIMIT = 200000.0;

    private AccountDAO accountDAO = new AccountDAO();
    private TransactionService transactionService = new TransactionService();

    // Both sides are saved in one database transaction: either both happen or neither does.
    public void transfer(int fromAccNo, int toAccNo, double amount, String note) throws BankException {
        if (!Validator.isValidAmount(amount)) {
            throw new InvalidAmountException("Amount must be at least Rs. 0.01.");
        }
        if (amount > TRANSFER_LIMIT) {
            throw new InvalidAmountException("Single transfer limit is " + MoneyFormatter.format(TRANSFER_LIMIT) + ".");
        }
        if (fromAccNo == toAccNo) {
            throw new BankException("You cannot transfer money to the same account.");
        }

        Account from = accountDAO.findByAccNo(fromAccNo);
        if (from == null) {
            throw new AccountNotFoundException(fromAccNo);
        }
        Account to = accountDAO.findByAccNo(toAccNo);
        if (to == null) {
            throw new AccountNotFoundException(toAccNo);
        }

        String suffix = "";
        if (note != null && !note.isEmpty()) {
            suffix = " - " + (note.length() > 40 ? note.substring(0, 40) : note);
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            transactionService.postWithin(con, from, -amount, TransactionType.TRANSFER_OUT,
                    "Transfer to " + toAccNo + suffix);
            transactionService.postWithin(con, to, amount, TransactionType.TRANSFER_IN,
                    "Transfer from " + fromAccNo + suffix);

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
}
