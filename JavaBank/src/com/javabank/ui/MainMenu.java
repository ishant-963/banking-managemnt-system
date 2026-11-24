package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.AccountType;
import com.javabank.model.Admin;
import com.javabank.model.Customer;
import com.javabank.service.AccountService;
import com.javabank.service.AuthService;
import com.javabank.util.ConsoleUtil;
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

            int choice = ConsoleUtil.readInt("\nEnter your choice: ");
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

        String name = ConsoleUtil.readLine("Full name: ");
        while (!Validator.isValidName(name)) {
            ConsoleUtil.error("Name must have only letters and spaces (2 to 50 characters).");
            name = ConsoleUtil.readLine("Full name: ");
        }

        String phone = ConsoleUtil.readLine("Mobile number (10 digits): ");
        while (!Validator.isValidPhone(phone)) {
            ConsoleUtil.error("Enter a valid 10 digit mobile number.");
            phone = ConsoleUtil.readLine("Mobile number (10 digits): ");
        }

        String email = ConsoleUtil.readLine("Email: ");
        while (!Validator.isValidEmail(email)) {
            ConsoleUtil.error("That email address does not look right.");
            email = ConsoleUtil.readLine("Email: ");
        }

        String address = ConsoleUtil.readLine("Address: ");
        while (!Validator.isValidAddress(address)) {
            ConsoleUtil.error("Address must be between 5 and 120 characters.");
            address = ConsoleUtil.readLine("Address: ");
        }

        System.out.println();
        AccountType type = askAccountType();
        String pin = askNewPin();
        double opening = askOpeningDeposit(type);

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
        int accNo = ConsoleUtil.readInt("Account number: ");
        String pin = ConsoleUtil.readLine("PIN: ");

        try {
            Account account = authService.loginCustomer(accNo, pin);
            new CustomerMenu(account, authService).show();
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void adminLogin() {
        ConsoleUtil.header("ADMIN LOGIN");
        String username = ConsoleUtil.readLine("Username: ");
        String password = ConsoleUtil.readLine("Password: ");

        try {
            Admin admin = authService.loginAdmin(username, password);
            new AdminMenu(admin).show();
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    static AccountType askAccountType() {
        while (true) {
            System.out.println("Account type:");
            System.out.println("  1. Savings  (4% interest, minimum balance Rs. 500)");
            System.out.println("  2. Current  (overdraft up to Rs. 10,000, no interest)");
            int choice = ConsoleUtil.readInt("Choose 1 or 2: ");
            if (choice == 1) return AccountType.SAVINGS;
            if (choice == 2) return AccountType.CURRENT;
            ConsoleUtil.error("Please choose 1 or 2.");
        }
    }

    static String askNewPin() {
        while (true) {
            String pin = ConsoleUtil.readLine("Choose a 4-digit PIN: ");
            if (!Validator.isValidPin(pin)) {
                ConsoleUtil.error("PIN must be exactly 4 digits.");
                continue;
            }
            String again = ConsoleUtil.readLine("Re-enter the PIN: ");
            if (!pin.equals(again)) {
                ConsoleUtil.error("The PINs did not match, try again.");
                continue;
            }
            return pin;
        }
    }

    static double askOpeningDeposit(AccountType type) {
        double minimum = Account.getMinimumOpeningDeposit(type);
        while (true) {
            double amount = ConsoleUtil.readDouble("Opening deposit (minimum "
                    + ConsoleUtil.formatMoney(minimum) + "): ");
            if (amount >= minimum && Validator.isValidAmount(amount)) {
                return amount;
            }
            ConsoleUtil.error("Opening deposit must be at least " + ConsoleUtil.formatMoney(minimum) + ".");
        }
    }
}
