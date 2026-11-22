package com.javabank.service;

import com.javabank.dao.AccountDAO;
import com.javabank.dao.CustomerDAO;
import com.javabank.dao.TransactionDAO;
import com.javabank.exception.AccountNotFoundException;
import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.Customer;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.TablePrinter;

import java.util.ArrayList;
import java.util.HashMap;

public class StatementService {

    private AccountDAO accountDAO = new AccountDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    public String generateStatement(int accNo) throws BankException {
        Account account = accountDAO.findByAccNo(accNo);
        if (account == null) {
            throw new AccountNotFoundException(accNo);
        }
        Customer customer = customerDAO.findById(account.getCustomerId());
        ArrayList<Transaction> transactions = transactionDAO.findAllByAccount(accNo);

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("ACCOUNT STATEMENT\n");
        sb.append("Customer     : ").append(customer.getName()).append("\n");
        sb.append("Account No   : ").append(account.getAccNo()).append(" (").append(account.getType()).append(")\n");
        sb.append("Status       : ").append(account.getStatus()).append("\n");
        sb.append("Opened on    : ").append(MoneyFormatter.date(account.getOpenedOn())).append("\n");
        sb.append("Balance      : ").append(MoneyFormatter.format(account.getBalance())).append("\n\n");
        sb.append(TablePrinter.transactionTable(transactions));

        if (!transactions.isEmpty()) {
            sb.append("\nSummary by type\n");
            HashMap<TransactionType, Double> totals = totalsByType(transactions);
            for (TransactionType type : TransactionType.values()) {
                if (totals.containsKey(type)) {
                    sb.append(String.format("  %-15s %s%n", type, MoneyFormatter.format(totals.get(type))));
                }
            }
        }
        return sb.toString();
    }

    // adds up the amounts for each kind of transaction
    public HashMap<TransactionType, Double> totalsByType(ArrayList<Transaction> transactions) {
        HashMap<TransactionType, Double> totals = new HashMap<>();
        for (Transaction t : transactions) {
            double current = totals.getOrDefault(t.getType(), 0.0);
            totals.put(t.getType(), MoneyFormatter.round(current + t.getAmount()));
        }
        return totals;
    }
}
