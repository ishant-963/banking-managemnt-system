package com.javabank.exception;

public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(int accNo) {
        super("Account " + accNo + " was not found.");
    }
}
