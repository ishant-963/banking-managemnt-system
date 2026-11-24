package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.Admin;
import com.javabank.model.Customer;
import com.javabank.service.AccountService;
import com.javabank.service.LoanService;
import com.javabank.service.ReportService;
import com.javabank.service.TransactionService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.TablePrinter;

import java.util.ArrayList;

public class AdminMenu {

    private Admin admin;

    private AccountService accountService = new AccountService();
    private TransactionService transactionService = new TransactionService();
    private LoanService loanService = new LoanService();
    private ReportService reportService = new ReportService();

    public AdminMenu(Admin admin) {
        this.admin = admin;
    }

    public void show() {
        ConsoleUtil.success("Admin login successful. Hello, " + admin.getUsername() + "!");

        boolean loggedIn = true;
        while (loggedIn) {
            ConsoleUtil.header("ADMIN PANEL");
            System.out.println(" 1. View all customers");
            System.out.println(" 2. View all accounts");
            System.out.println(" 3. Search an account");
            System.out.println(" 4. Freeze an account");
            System.out.println(" 5. Unfreeze an account");
            System.out.println(" 6. Close an account");
            System.out.println(" 7. Apply monthly interest (savings)");
            System.out.println(" 8. Recent transactions");
            System.out.println(" 9. View all loans");
            System.out.println("10. Bank summary report");
            System.out.println(" 0. Logout");

            int choice = ConsoleUtil.readInt("\nEnter your choice: ");
            switch (choice) {
                case 1:
                    viewCustomers();
                    break;
                case 2:
                    viewAccounts();
                    break;
                case 3:
                    searchAccount();
                    break;
                case 4:
                    freezeAccount();
                    break;
                case 5:
                    unfreezeAccount();
                    break;
                case 6:
                    closeAccount();
                    break;
                case 7:
                    applyInterest();
                    break;
                case 8:
                    recentTransactions();
                    break;
                case 9:
                    viewLoans();
                    break;
                case 10:
                    showSummary();
                    break;
                case 0:
                    ConsoleUtil.info("Admin logged out.");
                    loggedIn = false;
                    break;
                default:
                    ConsoleUtil.error("Invalid choice, please try again.");
            }
        }
    }

    private void viewCustomers() {
        try {
            System.out.println();
            System.out.print(TablePrinter.customerTable(accountService.getAllCustomers()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void viewAccounts() {
        try {
            System.out.println();
            System.out.print(TablePrinter.accountTable(accountService.getAllAccounts()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void searchAccount() {
        int accNo = ConsoleUtil.readInt("Account number: ");
        try {
            Account account = accountService.getAccount(accNo);
            Customer owner = accountService.getCustomer(account.getCustomerId());

            ArrayList<Account> single = new ArrayList<>();
            single.add(account);

            System.out.println("\nOwner   : " + owner.getName() + " (customer " + owner.getCustomerId() + ")");
            System.out.println("Phone   : " + owner.getPhone());
            System.out.println("Email   : " + owner.getEmail());
            System.out.println("Address : " + owner.getAddress() + "\n");
            System.out.print(TablePrinter.accountTable(single));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void freezeAccount() {
        int accNo = ConsoleUtil.readInt("Account number to freeze: ");
        try {
            accountService.freezeAccount(accNo);
            ConsoleUtil.success("Account " + accNo + " is now frozen.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void unfreezeAccount() {
        int accNo = ConsoleUtil.readInt("Account number to unfreeze: ");
        try {
            accountService.unfreezeAccount(accNo);
            ConsoleUtil.success("Account " + accNo + " is active again.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void closeAccount() {
        int accNo = ConsoleUtil.readInt("Account number to close: ");
        if (!ConsoleUtil.confirm("Closing an account cannot be undone. Continue?")) {
            ConsoleUtil.info("Cancelled.");
            return;
        }
        try {
            accountService.closeAccount(accNo);
            ConsoleUtil.success("Account " + accNo + " has been closed.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void applyInterest() {
        if (!ConsoleUtil.confirm("Credit this month's interest to all savings accounts?")) {
            ConsoleUtil.info("Cancelled.");
            return;
        }
        try {
            ArrayList<String> lines = transactionService.applyMonthlyInterest();
            if (lines.isEmpty()) {
                ConsoleUtil.info("Nothing to credit (interest may already be paid for this month).");
                return;
            }

            int count = lines.size() - 1;
            for (int i = 0; i < count; i++) {
                System.out.println(lines.get(i));
            }
            String totalText = lines.get(count).replace("TOTAL:", "");
            ConsoleUtil.success("Interest credited to " + count + " account(s), total " + totalText);
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void recentTransactions() {
        int count = ConsoleUtil.readInt("How many recent transactions to show: ");
        try {
            System.out.println();
            System.out.print(TablePrinter.transactionTable(transactionService.getRecentTransactions(count)));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void viewLoans() {
        try {
            System.out.println();
            System.out.print(TablePrinter.loanTable(loanService.getAllLoans()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void showSummary() {
        try {
            ConsoleUtil.header("BANK SUMMARY");
            System.out.println(reportService.buildBankSummary());
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }
}
