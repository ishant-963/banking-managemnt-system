package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountType;
import com.javabank.model.Customer;
import com.javabank.service.AccountService;
import com.javabank.service.AuthService;
import com.javabank.service.CustomerService;
import com.javabank.service.StatementService;
import com.javabank.service.TransactionService;
import com.javabank.service.TransferService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.InputHelper;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.TablePrinter;

public class CustomerMenu {

    private Account account;
    private AuthService authService;

    private AccountService accountService = new AccountService();
    private CustomerService customerService = new CustomerService();
    private TransactionService transactionService = new TransactionService();
    private TransferService transferService = new TransferService();
    private StatementService statementService = new StatementService();

    public CustomerMenu(Account account, AuthService authService) {
        this.account = account;
        this.authService = authService;
    }

    public void show() {
        try {
            Customer customer = customerService.getCustomer(account.getCustomerId());
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

            int choice = InputHelper.readInt("\nEnter your choice: ");
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
                    new ProfileMenu(account, authService).show();
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
        System.out.println("Current balance: " + MoneyFormatter.format(account.getBalance()));
        System.out.println(account.getRulesSummary());
    }

    private void deposit() {
        double amount = InputHelper.readDouble("Amount to deposit: ");
        try {
            account = transactionService.deposit(account.getAccNo(), amount);
            ConsoleUtil.success("Deposited " + MoneyFormatter.format(amount)
                    + ". New balance: " + MoneyFormatter.format(account.getBalance()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void withdraw() {
        double amount = InputHelper.readDouble("Amount to withdraw: ");
        try {
            account = transactionService.withdraw(account.getAccNo(), amount);
            ConsoleUtil.success("Withdrew " + MoneyFormatter.format(amount)
                    + ". New balance: " + MoneyFormatter.format(account.getBalance()));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void transfer() {
        int toAccNo = InputHelper.readInt("Receiver's account number: ");
        try {
            // show who is going to receive the money before asking for the amount
            Account receiver = accountService.getAccount(toAccNo);
            Customer receiverOwner = customerService.getCustomer(receiver.getCustomerId());
            System.out.println("Receiver: " + receiverOwner.getName() + " (" + receiver.getType() + " account)");

            double amount = InputHelper.readDouble("Amount to transfer: ");
            String note = InputHelper.readLine("Note (optional): ");

            if (!InputHelper.confirm("Send " + MoneyFormatter.format(amount) + " to " + receiverOwner.getName() + "?")) {
                ConsoleUtil.info("Transfer cancelled.");
                return;
            }

            transferService.transfer(account.getAccNo(), toAccNo, amount, note);
            refreshAccount();
            ConsoleUtil.success("Transfer complete. New balance: " + MoneyFormatter.format(account.getBalance()));
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
            System.out.println(statementService.generateStatement(account.getAccNo()));
            ConsoleUtil.pause();
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void openAnotherAccount() {
        ConsoleUtil.header("OPEN ANOTHER ACCOUNT");
        AccountType type = AccountForm.askAccountType();
        String pin = AccountForm.askNewPin();
        double opening = AccountForm.askOpeningDeposit(type, accountService);

        try {
            Account created = accountService.openAccount(account.getCustomerId(), type, pin, opening);
            ConsoleUtil.success("New " + type + " account opened. Account number: " + created.getAccNo());
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
