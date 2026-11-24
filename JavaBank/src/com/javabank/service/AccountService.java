package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.CustomerDAO;
import com.javabank.dao.LoanDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.BankException;
import com.javabank.exception.DatabaseException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.AccountType;
import com.javabank.model.Customer;
import com.javabank.model.Loan;
import com.javabank.model.LoanStatus;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.DBConnection;
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
        getCustomer(customerId);

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
            throw BankException.accountNotFound(accNo);
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
        return ConsoleUtil.roundMoney(total);
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
                    + ConsoleUtil.formatMoney(account.getBalance()));
        }
        for (Loan loan : loanDAO.findByCustomer(account.getCustomerId())) {
            if (loan.getAccNo() == accNo && loan.getStatus() == LoanStatus.ACTIVE) {
                throw new BankException("Account " + accNo + " still has an active loan (#" + loan.getLoanId() + ").");
            }
        }
        accountDAO.updateStatus(accNo, AccountStatus.CLOSED);
    }

    public Customer getCustomer(int customerId) throws BankException {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new BankException("Customer " + customerId + " was not found.");
        }
        return customer;
    }

    public ArrayList<Customer> getAllCustomers() throws BankException {
        return customerDAO.findAll();
    }

    public void updateContact(int customerId, String phone, String email, String address) throws BankException {
        validateContact(phone, email, address);
        customerDAO.updateContact(customerId, phone, email, address);
    }

    private int createAccountRows(Connection con, int customerId, AccountType type, String pin, double opening)
            throws BankException {
        int accNo = accountDAO.insert(con, customerId, type, Validator.hashPin(pin), opening);
        transactionDAO.insert(con, new Transaction(accNo, TransactionType.DEPOSIT, opening, opening, "Opening deposit"));
        return accNo;
    }

    private void validateCustomer(Customer c) throws BankException {
        if (!Validator.isValidName(c.getName())) {
            throw new BankException("Name must have only letters and spaces (2 to 50 characters).");
        }
        validateContact(c.getPhone(), c.getEmail(), c.getAddress());
    }

    private void validateContact(String phone, String email, String address) throws BankException {
        if (!Validator.isValidPhone(phone)) {
            throw new BankException("Phone must be a valid 10 digit mobile number.");
        }
        if (!Validator.isValidEmail(email)) {
            throw new BankException("That email address does not look right.");
        }
        if (!Validator.isValidAddress(address)) {
            throw new BankException("Address must be between 5 and 120 characters.");
        }
    }

    private void validateNewAccount(AccountType type, String pin, double openingDeposit) throws BankException {
        if (!Validator.isValidPin(pin)) {
            throw new BankException("PIN must be exactly 4 digits.");
        }
        double minimum = Account.getMinimumOpeningDeposit(type);
        if (!Validator.isValidAmount(openingDeposit) || openingDeposit < minimum) {
            throw new BankException("Minimum opening deposit for a " + type + " account is "
                    + ConsoleUtil.formatMoney(minimum) + ".");
        }
    }
}
