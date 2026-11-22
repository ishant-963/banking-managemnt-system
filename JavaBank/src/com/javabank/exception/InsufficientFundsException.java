package com.javabank.exception;

import com.javabank.model.Account;
import com.javabank.util.MoneyFormatter;

public class InsufficientFundsException extends BankException {

    public InsufficientFundsException(Account account, double requested) {
        super("Insufficient funds. Cannot take out " + MoneyFormatter.format(requested)
                + " (balance is " + MoneyFormatter.format(account.getBalance()) + "). "
                + account.getRulesSummary());
    }
}
