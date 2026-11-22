package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.AdminDAO;
import com.javabank.exception.AccountNotFoundException;
import com.javabank.exception.AuthenticationException;
import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.Admin;
import com.javabank.util.PinHasher;
import com.javabank.util.Validator;

import java.util.HashMap;

public class AuthService {

    private static final int MAX_ATTEMPTS = 3;

    private AccountDAO accountDAO = new AccountDAO();
    private AdminDAO adminDAO = new AdminDAO();

    // account number -> number of wrong PIN attempts so far
    private HashMap<Integer, Integer> failedAttempts = new HashMap<>();

    public Account loginCustomer(int accNo, String pin) throws BankException {
        int failures = failedAttempts.getOrDefault(accNo, 0);
        if (failures >= MAX_ATTEMPTS) {
            throw new AuthenticationException("Too many wrong PIN attempts. This account is locked until the program restarts.");
        }

        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw new AccountNotFoundException(accNo);
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AuthenticationException("This account has been closed.");
        }

        String storedHash = accountDAO.getPinHash(accNo);
        if (!PinHasher.hash(pin).equals(storedHash)) {
            failedAttempts.put(accNo, failures + 1);
            int left = MAX_ATTEMPTS - failures - 1;
            throw new AuthenticationException("Incorrect PIN. Attempts left: " + left);
        }

        failedAttempts.remove(accNo);
        return account;
    }

    public Admin loginAdmin(String username, String password) throws BankException {
        Admin admin = adminDAO.findByCredentials(username, PinHasher.hash(password));
        if (admin == null) {
            throw new AuthenticationException("Wrong admin username or password.");
        }
        return admin;
    }

    public void changePin(int accNo, String oldPin, String newPin) throws BankException {
        String storedHash = accountDAO.getPinHash(accNo);
        if (storedHash == null) {
            throw new AccountNotFoundException(accNo);
        }
        if (!PinHasher.hash(oldPin).equals(storedHash)) {
            throw new AuthenticationException("Your current PIN is incorrect.");
        }
        if (!Validator.isValidPin(newPin)) {
            throw new BankException("The new PIN must be exactly 4 digits.");
        }
        if (oldPin.equals(newPin)) {
            throw new BankException("The new PIN must be different from the old one.");
        }
        accountDAO.updatePin(accNo, PinHasher.hash(newPin));
    }
}
