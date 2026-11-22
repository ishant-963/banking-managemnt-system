package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountType;
import com.javabank.model.Admin;
import com.javabank.model.Customer;
import com.javabank.service.AccountService;
import com.javabank.service.AuthService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.InputHelper;
import com.javabank.util.Validator;

public class MainMenu {

    private AuthService authService = new AuthService();
    private AccountService accountService = new AccountService();

    public void start() {
        boolean running = true;
        while (running) {
            ConsoleUtil.header("MAIN MENU");
            System.out.println("1. Open a new account (new customer)");
            System.out.println("2. Customer login");
            System.out.println("3. Admin login");
            System.out.println("0. Exit");

            int choice = InputHelper.readInt("\nEnter your choice: ");
            switch (choice) {
                case 1:
                    registerNewCustomer();
                    break;
                case 2:
                    customerLogin();
                    break;
                case 3:
                    adminLogin();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    ConsoleUtil.error("Invalid choice, please try again.");
            }
        }
    }

    private void registerNewCustomer() {
        ConsoleUtil.header("NEW CUSTOMER REGISTRATION");

        String name = InputHelper.readLine("Full name: ");
        while (!Validator.isValidName(name)) {
            ConsoleUtil.error("Name must have only letters and spaces (2 to 50 characters).");
            name = InputHelper.readLine("Full name: ");
        }

        String phone = InputHelper.readLine("Mobile number (10 digits): ");
        while (!Validator.isValidPhone(phone)) {
            ConsoleUtil.error("Enter a valid 10 digit mobile number.");
            phone = InputHelper.readLine("Mobile number (10 digits): ");
        }

        String email = InputHelper.readLine("Email: ");
        while (!Validator.isValidEmail(email)) {
            ConsoleUtil.error("That email address does not look right.");
            email = InputHelper.readLine("Email: ");
        }

        String address = InputHelper.readLine("Address: ");
        while (!Validator.isValidAddress(address)) {
            ConsoleUtil.error("Address must be between 5 and 120 characters.");
            address = InputHelper.readLine("Address: ");
        }

        System.out.println();
        AccountType type = AccountForm.askAccountType();
        String pin = AccountForm.askNewPin();
        double opening = AccountForm.askOpeningDeposit(type, accountService);

        try {
            Customer customer = new Customer(name, phone, email, address);
            Account account = accountService.openAccountWithNewCustomer(customer, type, pin, opening);
            ConsoleUtil.success("Welcome to Java Bank, " + name + "!");
            System.out.println("Your account number is: " + account.getAccNo());
            System.out.println("Please remember it, you will need it (and your PIN) to log in.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void customerLogin() {
        ConsoleUtil.header("CUSTOMER LOGIN");
        int accNo = InputHelper.readInt("Account number: ");
        String pin = InputHelper.readLine("PIN: ");

        try {
            Account account = authService.loginCustomer(accNo, pin);
            new CustomerMenu(account, authService).show();
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void adminLogin() {
        ConsoleUtil.header("ADMIN LOGIN");
        String username = InputHelper.readLine("Username: ");
        String password = InputHelper.readLine("Password: ");

        try {
            Admin admin = authService.loginAdmin(username, password);
            new AdminMenu(admin).show();
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }
}
