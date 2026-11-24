package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.CustomerDAO;
import com.javabank.dao.LoanDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.AccountType;
import com.javabank.model.Customer;
import com.javabank.model.Loan;
import com.javabank.model.LoanStatus;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.TablePrinter;

import java.util.ArrayList;

public class ReportService {

    private CustomerDAO customerDAO = new CustomerDAO();
    private AccountDAO accountDAO = new AccountDAO();
    private LoanDAO loanDAO = new LoanDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    public String buildBankSummary() throws BankException {
        int customerCount = customerDAO.findAll().size();
        ArrayList<Account> accounts = accountDAO.findAll();
        ArrayList<Loan> loans = loanDAO.findAll();

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
            int count = 0;
            double totalBalance = 0;
            for (Account a : accounts) {
                if (a.getType() == type) {
                    count++;
                    totalBalance += a.getBalance();
                }
            }
            sb.append(String.format("  %-9s %3d accounts, total balance %s%n",
                    type, count, ConsoleUtil.formatMoney(totalBalance)));
        }

        sb.append("\nAccounts by status\n");
        for (AccountStatus status : AccountStatus.values()) {
            int count = 0;
            for (Account a : accounts) {
                if (a.getStatus() == status) {
                    count++;
                }
            }
            sb.append(String.format("  %-9s %3d%n", status, count));
        }

        sb.append("\nLoans\n");
        sb.append("  Active loans        : ").append(activeLoans).append("\n");
        sb.append("  Amount outstanding  : ").append(ConsoleUtil.formatMoney(outstanding)).append("\n");
        return sb.toString();
    }

    public String generateStatement(int accNo) throws BankException {
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw BankException.accountNotFound(accNo);
        }
        Customer customer = customerDAO.findById(account.getCustomerId());
        ArrayList<Transaction> transactions = transactionDAO.findAllByAccount(accNo);

        StringBuilder sb = new StringBuilder();
        sb.append("\nACCOUNT STATEMENT\n");
        sb.append("Customer     : ").append(customer.getName()).append("\n");
        sb.append("Account No   : ").append(account.getAccNo()).append(" (").append(account.getType()).append(")\n");
        sb.append("Status       : ").append(account.getStatus()).append("\n");
        sb.append("Opened on    : ").append(ConsoleUtil.formatDate(account.getOpenedOn())).append("\n");
        sb.append("Balance      : ").append(ConsoleUtil.formatMoney(account.getBalance())).append("\n\n");
        sb.append(TablePrinter.transactionTable(transactions));

        if (!transactions.isEmpty()) {
            sb.append("\nSummary by type\n");
            for (TransactionType type : TransactionType.values()) {
                double total = 0;
                boolean found = false;
                for (Transaction t : transactions) {
                    if (t.getType() == type) {
                        total = ConsoleUtil.roundMoney(total + t.getAmount());
                        found = true;
                    }
                }
                if (found) {
                    sb.append(String.format("  %-15s %s%n", type, ConsoleUtil.formatMoney(total)));
                }
            }
        }
        return sb.toString();
    }
}
