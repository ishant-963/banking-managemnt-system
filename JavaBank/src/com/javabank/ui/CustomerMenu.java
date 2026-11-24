package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountType;
import com.javabank.model.Customer;
import com.javabank.service.AccountService;
import com.javabank.service.AuthService;
import com.javabank.service.ReportService;
import com.javabank.service.TransactionService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.TablePrinter;

public class CustomerMenu {

    private Account account;
    private AuthService authService;

    private AccountService accountService = new AccountService();
    private TransactionService transactionService = new TransactionService();
    private ReportService reportService = new ReportService();

    public CustomerMenu(Account account, AuthService authService) {
        this.account = account;
        this.authService = authService;
    }

    public void show() {
        try {
            Customer customer = accountService.getCustomer(account.getCustomerId());
            ConsoleUtil.success("Login successful. Welcome back, " + customer.getName() + "!");
        } catch (BankException e) {
            ConsoleUtil.success("Login successful.");
        }

        boolean loggedIn = true;
        while (loggedIn) {
            ConsoleUtil.header("ACCOUNT " + account.getAccNo() + " (" + account.getType() + ")");
            System.out.println("1. Check balance");
            System.out.println("2. Deposit money");
            System.out.println("3. Withdraw money");
            System.out.println("4. Transfer money");
            System.out.println("5. Mini statement (last 5)");
            System.out.println("6. Full statement");
            System.out.println("7. Open another account");
            System.out.println("8. Loans");
            System.out.println("9. Profile and settings");
            System.out.println("0. Logout");

            int choice = ConsoleUtil.readInt("\nEnter your choice: ");
            switch (choice) {
                case 1:
                    checkBalance();
                    break;
                case 2:
                    deposit();
                    break;
                case 3:
                    withdraw();
                    break;
                case 4:
                    transfer();
                    break;
                case 5:
                    miniStatement();
                    break;
                case 6:
                    fullStatement();
                    break;
                case 7:
                    openAnotherAccount();
                    break;
                case 8:
                    new LoanMenu(account).show();
                    refreshAccount();
                    break;
                case 9:
                    showProfileMenu();
                    break;
                case 0:
                    ConsoleUtil.info("Logged out. See you again!");
                    loggedIn = false;
                    break;
                default:
                    ConsoleUtil.error("Invalid choice, please try again.");
            }
        }
    }

    private void checkBalance() {
        refreshAccount();
        System.out.println("Account status : " + account.getStatus());
        System.out.println("Current balance: " + ConsoleUtil.formatMoney(account.getBalance()));
        System.out.println(account.getRulesSummary());
    }

    private void deposit() {
        double amount = ConsoleUtil.readDouble("Amount to deposit: ");
        try {
            account = transactionService.deposit(account.getAccNo(), amount);
            ConsoleUtil.success("Deposited " + ConsoleUtil.formatMoney(amount)
                    + ". New balance: " + ConsoleUtil.formatMoney(account.getBalance()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void withdraw() {
        double amount = ConsoleUtil.readDouble("Amount to withdraw: ");
        try {
            account = transactionService.withdraw(account.getAccNo(), amount);
            ConsoleUtil.success("Withdrew " + ConsoleUtil.formatMoney(amount)
                    + ". New balance: " + ConsoleUtil.formatMoney(account.getBalance()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void transfer() {
        int toAccNo = ConsoleUtil.readInt("Receiver's account number: ");
        try {
            Account receiver = accountService.getAccount(toAccNo);
            Customer receiverOwner = accountService.getCustomer(receiver.getCustomerId());
            System.out.println("Receiver: " + receiverOwner.getName() + " (" + receiver.getType() + " account)");

            double amount = ConsoleUtil.readDouble("Amount to transfer: ");
            String note = ConsoleUtil.readLine("Note (optional): ");

            if (!ConsoleUtil.confirm("Send " + ConsoleUtil.formatMoney(amount) + " to " + receiverOwner.getName() + "?")) {
                ConsoleUtil.info("Transfer cancelled.");
                return;
            }

            transactionService.transfer(account.getAccNo(), toAccNo, amount, note);
            refreshAccount();
            ConsoleUtil.success("Transfer complete. New balance: " + ConsoleUtil.formatMoney(account.getBalance()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void miniStatement() {
        try {
            System.out.println();
            System.out.print(TablePrinter.transactionTable(transactionService.getMiniStatement(account.getAccNo())));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void fullStatement() {
        try {
            System.out.println(reportService.generateStatement(account.getAccNo()));
            ConsoleUtil.pause();
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void openAnotherAccount() {
        ConsoleUtil.header("OPEN ANOTHER ACCOUNT");
        AccountType type = MainMenu.askAccountType();
        String pin = MainMenu.askNewPin();
        double opening = MainMenu.askOpeningDeposit(type);

        try {
            Account created = accountService.openAccount(account.getCustomerId(), type, pin, opening);
            ConsoleUtil.success("New " + type + " account opened. Account number: " + created.getAccNo());
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void showProfileMenu() {
        boolean inMenu = true;
        while (inMenu) {
            ConsoleUtil.header("PROFILE AND SETTINGS");
            System.out.println("1. View my profile and accounts");
            System.out.println("2. Update contact details");
            System.out.println("3. Change PIN");
            System.out.println("0. Back");

            int choice = ConsoleUtil.readInt("\nEnter your choice: ");
            switch (choice) {
                case 1:
                    viewProfile();
                    break;
                case 2:
                    updateContact();
                    break;
                case 3:
                    changePin();
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    ConsoleUtil.error("Invalid choice, please try again.");
            }
        }
    }

    private void viewProfile() {
        try {
            Customer c = accountService.getCustomer(account.getCustomerId());
            System.out.println("\nCustomer ID : " + c.getCustomerId());
            System.out.println("Name        : " + c.getName());
            System.out.println("Phone       : " + c.getPhone());
            System.out.println("Email       : " + c.getEmail());
            System.out.println("Address     : " + c.getAddress());

            System.out.println("\nYour accounts:");
            System.out.print(TablePrinter.accountTable(accountService.getAccountsOfCustomer(c.getCustomerId())));
            System.out.println("Total across all accounts: "
                    + ConsoleUtil.formatMoney(accountService.getTotalBalance(c.getCustomerId())));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void updateContact() {
        try {
            Customer c = accountService.getCustomer(account.getCustomerId());
            System.out.println("Leave a field empty to keep the current value.");

            String phone = ConsoleUtil.readLine("Phone [" + c.getPhone() + "]: ");
            String email = ConsoleUtil.readLine("Email [" + c.getEmail() + "]: ");
            String address = ConsoleUtil.readLine("Address [" + c.getAddress() + "]: ");

            if (phone.isEmpty()) phone = c.getPhone();
            if (email.isEmpty()) email = c.getEmail();
            if (address.isEmpty()) address = c.getAddress();

            accountService.updateContact(c.getCustomerId(), phone, email, address);
            ConsoleUtil.success("Contact details updated.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void changePin() {
        String oldPin = ConsoleUtil.readLine("Current PIN: ");
        String newPin = MainMenu.askNewPin();
        try {
            authService.changePin(account.getAccNo(), oldPin, newPin);
            ConsoleUtil.success("PIN changed successfully.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    // re-reads the account so the balance on screen is always the real one
    private void refreshAccount() {
        try {
            account = accountService.getAccount(account.getAccNo());
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }
}
