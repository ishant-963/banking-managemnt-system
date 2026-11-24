package com.javabank.util;

import com.javabank.model.Account;
import com.javabank.model.Customer;
import com.javabank.model.Loan;
import com.javabank.model.Transaction;

import java.util.ArrayList;

// Builds nicely aligned text tables. Each method returns a String so the
// caller decides whether to print it or put it in a statement.
public class TablePrinter {

    public static String customerTable(ArrayList<Customer> customers) {
        if (customers.isEmpty()) {
            return "No customers found.\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-22s %-12s %-28s%n", "ID", "Name", "Phone", "Email"));
        sb.append("-".repeat(72)).append("\n");
        for (Customer c : customers) {
            sb.append(String.format("%-6d %-22s %-12s %-28s%n",
                    c.getCustomerId(), c.getName(), c.getPhone(), c.getEmail()));
        }
        return sb.toString();
    }

    public static String accountTable(ArrayList<Account> accounts) {
        if (accounts.isEmpty()) {
            return "No accounts found.\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-9s %-9s %-9s %-16s %-8s %-17s%n",
                "Acc No", "Cust ID", "Type", "Balance", "Status", "Opened"));
        sb.append("-".repeat(74)).append("\n");
        for (Account a : accounts) {
            sb.append(String.format("%-9d %-9d %-9s %-16s %-8s %-17s%n",
                    a.getAccNo(), a.getCustomerId(), a.getType(),
                    ConsoleUtil.formatMoney(a.getBalance()), a.getStatus(),
                    ConsoleUtil.formatDate(a.getOpenedOn())));
        }
        return sb.toString();
    }

    public static String transactionTable(ArrayList<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "No transactions found.\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s %-8s %-17s %-15s %-15s %-15s %s%n",
                "ID", "Acc No", "Date", "Type", "Amount", "Balance", "Details"));
        sb.append("-".repeat(100)).append("\n");
        for (Transaction t : transactions) {
            sb.append(String.format("%-6d %-8d %-17s %-15s %-15s %-15s %s%n",
                    t.getTxnId(), t.getAccNo(), ConsoleUtil.formatDate(t.getTime()), t.getType(),
                    ConsoleUtil.formatMoney(t.getAmount()), ConsoleUtil.formatMoney(t.getBalanceAfter()),
                    t.getDescription()));
        }
        return sb.toString();
    }

    public static String loanTable(ArrayList<Loan> loans) {
        if (loans.isEmpty()) {
            return "No loans found.\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-7s %-8s %-15s %-6s %-7s %-13s %-15s %-7s%n",
                "Loan", "Acc No", "Principal", "Rate", "Months", "EMI", "Remaining", "Status"));
        sb.append("-".repeat(88)).append("\n");
        for (Loan l : loans) {
            sb.append(String.format("%-7d %-8d %-15s %-6s %-7d %-13s %-15s %-7s%n",
                    l.getLoanId(), l.getAccNo(), ConsoleUtil.formatMoney(l.getPrincipal()),
                    l.getAnnualRate() + "%", l.getMonths(), ConsoleUtil.formatMoney(l.getEmi()),
                    ConsoleUtil.formatMoney(l.getRemaining()), l.getStatus()));
        }
        return sb.toString();
    }
}
