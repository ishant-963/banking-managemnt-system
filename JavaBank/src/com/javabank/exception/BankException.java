package com.javabank.exception;

import com.javabank.model.Account;
import com.javabank.util.ConsoleUtil;

// Parent of all the errors the bank can throw, so menus can catch just this one.
public class BankException extends Exception {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }

    public static BankException accountNotFound(int accNo) {
        return new BankException("Account " + accNo + " was not found.");
    }

    public static BankException accountFrozen(int accNo) {
        return new BankException("Account " + accNo + " is not active (frozen or closed), so no transactions are allowed.");
    }

    public static BankException insufficientFunds(Account account, double requested) {
        return new BankException("Insufficient funds. Cannot take out " + ConsoleUtil.formatMoney(requested)
                + " (balance is " + ConsoleUtil.formatMoney(account.getBalance()) + "). "
                + account.getRulesSummary());
    }
}
