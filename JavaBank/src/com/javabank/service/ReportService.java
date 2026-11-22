package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.CustomerDAO;
import com.javabank.dao.LoanDAO;
import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.AccountType;
import com.javabank.model.Loan;
import com.javabank.model.LoanStatus;
import com.javabank.util.MoneyFormatter;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportService {

    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private LoanDAO loanDAO = new LoanDAO();

    public String buildBankSummary() throws BankException {
        int customerCount = customerDAO.findAll().size();
        ArrayList<Account> accounts = accountDAO.findAll();
        ArrayList<Loan> loans = loanDAO.findAll();

        HashMap<AccountType, Integer> countByType = new HashMap<>();
        HashMap<AccountType, Double> balanceByType = new HashMap<>();
        HashMap<AccountStatus, Integer> countByStatus = new HashMap<>();

        for (Account a : accounts) {
            countByType.put(a.getType(), countByType.getOrDefault(a.getType(), 0) + 1);
            balanceByType.put(a.getType(), balanceByType.getOrDefault(a.getType(), 0.0) + a.getBalance());
            countByStatus.put(a.getStatus(), countByStatus.getOrDefault(a.getStatus(), 0) + 1);
        }

        int activeLoans = 0;
        double outstanding = 0;
        for (Loan l : loans) {
            if (l.getStatus() == LoanStatus.ACTIVE) {
                activeLoans++;
                outstanding += l.getRemaining();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Total customers : ").append(customerCount).append("\n");
        sb.append("Total accounts  : ").append(accounts.size()).append("\n\n");

        sb.append("Accounts by type\n");
        for (AccountType type : AccountType.values()) {
            sb.append(String.format("  %-9s %3d accounts, total balance %s%n", type,
                    countByType.getOrDefault(type, 0),
                    MoneyFormatter.format(balanceByType.getOrDefault(type, 0.0))));
        }

        sb.append("\nAccounts by status\n");
        for (AccountStatus status : AccountStatus.values()) {
            sb.append(String.format("  %-9s %3d%n", status, countByStatus.getOrDefault(status, 0)));
        }

        sb.append("\nLoans\n");
        sb.append("  Active loans        : ").append(activeLoans).append("\n");
        sb.append("  Amount outstanding  : ").append(MoneyFormatter.format(outstanding)).append("\n");
        return sb.toString();
    }
}
