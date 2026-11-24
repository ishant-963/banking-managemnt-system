package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.AdminDAO;
import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.Admin;
import com.javabank.util.Validator;

import java.util.ArrayList;

public class AuthService {

    private static final int MAX_ATTEMPTS = 3;

    private AccountDAO accountDAO = new AccountDAO();
    private AdminDAO adminDAO = new AdminDAO();

    // tracks wrong PIN attempts using simple parallel lists
    private ArrayList<Integer> failedAccNos = new ArrayList<>();
    private ArrayList<Integer> failedCounts = new ArrayList<>();

    public Account loginCustomer(int accNo, String pin) throws BankException {
        int index = failedAccNos.indexOf(accNo);
        int failures = 0;
        if (index != -1) {
            failures = failedCounts.get(index);
        }
        if (failures >= MAX_ATTEMPTS) {
            throw new BankException("Too many wrong PIN attempts. This account is locked until the program restarts.");
        }

        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw BankException.accountNotFound(accNo);
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BankException("This account has been closed.");
        }

        String storedHash = accountDAO.getPinHash(accNo);
        if (!Validator.hashPin(pin).equals(storedHash)) {
            if (index == -1) {
                failedAccNos.add(accNo);
                failedCounts.add(1);
            } else {
                failedCounts.set(index, failures + 1);
            }
            int left = MAX_ATTEMPTS - failures - 1;
            throw new BankException("Incorrect PIN. Attempts left: " + left);
        }

        if (index != -1) {
            failedAccNos.remove(index);
            failedCounts.remove(index);
        }
        return account;
    }

    public Admin loginAdmin(String username, String password) throws BankException {
        Admin admin = adminDAO.findByCredentials(username, Validator.hashPin(password));
        if (admin == null) {
            throw new BankException("Wrong admin username or password.");
        }
        return admin;
    }

    public void changePin(int accNo, String oldPin, String newPin) throws BankException {
        String storedHash = accountDAO.getPinHash(accNo);
        if (storedHash == null) {
            throw BankException.accountNotFound(accNo);
        }
        if (!Validator.hashPin(oldPin).equals(storedHash)) {
            throw new BankException("Your current PIN is incorrect.");
        }
        if (!Validator.isValidPin(newPin)) {
            throw new BankException("The new PIN must be exactly 4 digits.");
        }
        if (oldPin.equals(newPin)) {
            throw new BankException("The new PIN must be different from the old one.");
        }
        accountDAO.updatePin(accNo, Validator.hashPin(newPin));
    }
}
