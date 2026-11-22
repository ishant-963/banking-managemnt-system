package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.Customer;
import com.javabank.service.AccountService;
import com.javabank.service.AuthService;
import com.javabank.service.CustomerService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.InputHelper;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.TablePrinter;

public class ProfileMenu {

    private Account account;
    private AuthService authService;
    private CustomerService customerService = new CustomerService();
    private AccountService accountService = new AccountService();

    public ProfileMenu(Account account, AuthService authService) {
        this.account = account;
        this.authService = authService;
    }

    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            ConsoleUtil.header("PROFILE AND SETTINGS");
            System.out.println("1. View my profile and accounts");
            System.out.println("2. Update contact details");
            System.out.println("3. Change PIN");
            System.out.println("0. Back");

            int choice = InputHelper.readInt("\nEnter your choice: ");
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
            Customer c = customerService.getCustomer(account.getCustomerId());
            System.out.println("\nCustomer ID : " + c.getCustomerId());
            System.out.println("Name        : " + c.getName());
            System.out.println("Phone       : " + c.getPhone());
            System.out.println("Email       : " + c.getEmail());
            System.out.println("Address     : " + c.getAddress());

            System.out.println("\nYour accounts:");
            System.out.print(TablePrinter.accountTable(accountService.getAccountsOfCustomer(c.getCustomerId())));
            System.out.println("Total across all accounts: "
                    + MoneyFormatter.format(accountService.getTotalBalance(c.getCustomerId())));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void updateContact() {
        try {
            Customer c = customerService.getCustomer(account.getCustomerId());
            System.out.println("Leave a field empty to keep the current value.");

            String phone = InputHelper.readLine("Phone [" + c.getPhone() + "]: ");
            String email = InputHelper.readLine("Email [" + c.getEmail() + "]: ");
            String address = InputHelper.readLine("Address [" + c.getAddress() + "]: ");

            if (phone.isEmpty()) {
                phone = c.getPhone();
            }
            if (email.isEmpty()) {
                email = c.getEmail();
            }
            if (address.isEmpty()) {
                address = c.getAddress();
            }

            customerService.updateContact(c.getCustomerId(), phone, email, address);
            ConsoleUtil.success("Contact details updated.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void changePin() {
        String oldPin = InputHelper.readLine("Current PIN: ");
        String newPin = AccountForm.askNewPin();
        try {
            authService.changePin(account.getAccNo(), oldPin, newPin);
            ConsoleUtil.success("PIN changed successfully.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }
}
