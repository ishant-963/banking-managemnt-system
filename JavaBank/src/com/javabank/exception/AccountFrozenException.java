package com.javabank.exception;

public class AccountFrozenException extends BankException {

    public AccountFrozenException(int accNo) {
        super("Account " + accNo + " is not active (frozen or closed), so no transactions are allowed.");
    }
}
