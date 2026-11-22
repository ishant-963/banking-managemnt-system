package com.javabank.exception;

// Parent of all the errors the bank can throw, so menus can catch just this one.
public class BankException extends Exception {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
