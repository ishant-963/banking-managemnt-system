package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.CustomerDAO;
import com.javabank.dao.LoanDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.AccountNotFoundException;
import com.javabank.exception.BankException;
import com.javabank.exception.DatabaseException;
import com.javabank.exception.InvalidAmountException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.AccountType;
import com.javabank.model.CurrentAccount;
import com.javabank.model.Customer;
import com.javabank.model.Loan;
import com.javabank.model.LoanStatus;
import com.javabank.model.SavingsAccount;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.DBConnection;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.PinHasher;
import com.javabank.util.Validator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class AccountService {

    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private LoanDAO loanDAO = new LoanDAO();

    // brand new customer: the customer row, account row and opening deposit are saved together
    public Account openAccountWithNewCustomer(Customer customer, AccountType type, String pin, double openingDeposit)
            throws BankException {
        validateCustomer(customer);
        validateNewAccount(type, pin, openingDeposit);

        Connection con = null;
        int accNo = -1;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int customerId = customerDAO.insert(con, customer);
            accNo = createAccountRows(con, customerId, type, pin, openingDeposit);

            con.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Could not open the account: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
        return getAccount(accNo);
    }

    // existing customer opening one more account
    public Account openAccount(int customerId, AccountType type, String pin, double openingDeposit)
            throws BankException {
        validateNewAccount(type, pin, openingDeposit);
        if (customerDAO.findById(customerId) == null) {
            throw new BankException("Customer " + customerId + " was not found.");
        }

        Connection con = null;
        int accNo = -1;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            accNo = createAccountRows(con, customerId, type, pin, openingDeposit);

            con.commit();
        } catch (SQLException e) {
            DBConnection.rollbackQuietly(con);
            throw new DatabaseException("Could not open the account: " + e.getMessage(), e);
        } catch (BankException e) {
            DBConnection.rollbackQuietly(con);
            throw e;
        } finally {
            DBConnection.closeQuietly(con);
        }
        return getAccount(accNo);
    }

    public Account getAccount(int accNo) throws BankException {
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw new AccountNotFoundException(accNo);
        }
        return account;
    }

    public ArrayList<Account> getAccountsOfCustomer(int customerId) throws BankException {
        return accountDAO.findByCustomer(customerId);
    }

    public ArrayList<Account> getAllAccounts() throws BankException {
        return accountDAO.findAll();
    }

    public double getTotalBalance(int customerId) throws BankException {
        double total = 0;
        for (Account account : accountDAO.findByCustomer(customerId)) {
            total += account.getBalance();
        }
        return MoneyFormatter.round(total);
    }

    public void freezeAccount(int accNo) throws BankException {
        Account account = getAccount(accNo);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankException("A closed account cannot be frozen.");
        }
        accountDAO.updateStatus(accNo, AccountStatus.FROZEN);
    }

    public void unfreezeAccount(int accNo) throws BankException {
        Account account = getAccount(accNo);
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new BankException("Account " + accNo + " is not frozen.");
        }
        accountDAO.updateStatus(accNo, AccountStatus.ACTIVE);
    }

    public void closeAccount(int accNo) throws BankException {
        Account account = getAccount(accNo);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankException("Account " + accNo + " is already closed.");
        }
        if (Math.abs(account.getBalance()) >= 0.01) {
            throw new BankException("Balance must be exactly zero before closing. Current balance: "
                    + MoneyFormatter.format(account.getBalance()));
        }
        // an account with a running loan cannot be closed
        for (Loan loan : loanDAO.findByCustomer(account.getCustomerId())) {
            if (loan.getAccNo() == accNo && loan.getStatus() == LoanStatus.ACTIVE) {
                throw new BankException("Account " + accNo + " still has an active loan (#" + loan.getLoanId() + ").");
            }
        }
        accountDAO.updateStatus(accNo, AccountStatus.CLOSED);
    }

    private int createAccountRows(Connection con, int customerId, AccountType type, String pin, double opening)
            throws BankException {
        int accNo = accountDAO.insert(con, customerId, type, PinHasher.hash(pin), opening);
        Transaction openingTxn = new Transaction(accNo, TransactionType.DEPOSIT, opening, opening, "Opening deposit");
        transactionDAO.insert(con, openingTxn);
        return accNo;
    }

    private void validateCustomer(Customer c) throws BankException {
        if (!Validator.isValidName(c.getName())) {
            throw new BankException("Name must have only letters and spaces (2 to 50 characters).");
        }
        if (!Validator.isValidPhone(c.getPhone())) {
            throw new BankException("Phone must be a valid 10 digit mobile number.");
        }
        if (!Validator.isValidEmail(c.getEmail())) {
            throw new BankException("That email address does not look right.");
        }
        if (!Validator.isValidAddress(c.getAddress())) {
            throw new BankException("Address must be between 5 and 120 characters.");
        }
    }

    private void validateNewAccount(AccountType type, String pin, double openingDeposit) throws BankException {
        if (!Validator.isValidPin(pin)) {
            throw new BankException("PIN must be exactly 4 digits.");
        }
        double minimum = getMinimumOpeningDeposit(type);
        if (!Validator.isValidAmount(openingDeposit) || openingDeposit < minimum) {
            throw new InvalidAmountException("Minimum opening deposit for a " + type + " account is "
                    + MoneyFormatter.format(minimum) + ".");
        }
    }

    public double getMinimumOpeningDeposit(AccountType type) {
        if (type == AccountType.SAVINGS) {
            return SavingsAccount.MIN_BALANCE;
        }
        return CurrentAccount.MIN_OPENING_DEPOSIT;
    }
}
