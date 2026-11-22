package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountType;
import com.javabank.model.TransactionType;

import java.util.HashMap;

public class InterestService {

    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();
    private TransactionService transactionService = new TransactionService();

    // Credits one month of interest to every active savings account.
    // Returns account number -> interest credited, so the menu can print a summary.
    public HashMap<Integer, Double> applyMonthlyInterest() throws BankException {
        HashMap<Integer, Double> credited = new HashMap<>();

        for (Account account : accountDAO.findByType(AccountType.SAVINGS)) {
            if (!account.isActive()) {
                continue;
            }
            if (transactionDAO.hasInterestThisMonth(account.getAccNo())) {
                continue; // already paid this month
            }

            double interest = account.calculateMonthlyInterest();
            if (interest <= 0) {
                continue;
            }

            try {
                transactionService.postSingle(account.getAccNo(), interest,
                        TransactionType.INTEREST, "Monthly interest");
                credited.put(account.getAccNo(), interest);
            } catch (BankException e) {
                // one bad account should not stop the rest, so we just skip it
            }
        }
        return credited;
    }
}
