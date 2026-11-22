package com.javabank.ui;

import com.javabank.model.AccountType;
import com.javabank.service.AccountService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.InputHelper;
import com.javabank.util.MoneyFormatter;
import com.javabank.util.Validator;

// The questions we ask whenever a new account is being opened.
// Used both for new customers and for existing customers opening another account.
public class AccountForm {

    public static AccountType askAccountType() {
        while (true) {
            System.out.println("Account type:");
            System.out.println("  1. Savings  (4% interest, minimum balance Rs. 500)");
            System.out.println("  2. Current  (overdraft up to Rs. 10,000, no interest)");
            int choice = InputHelper.readInt("Choose 1 or 2: ");
            if (choice == 1) {
                return AccountType.SAVINGS;
            }
            if (choice == 2) {
                return AccountType.CURRENT;
            }
            ConsoleUtil.error("Please choose 1 or 2.");
        }
    }

    public static String askNewPin() {
        while (true) {
            String pin = InputHelper.readLine("Choose a 4-digit PIN: ");
            if (!Validator.isValidPin(pin)) {
                ConsoleUtil.error("PIN must be exactly 4 digits.");
                continue;
            }
            String again = InputHelper.readLine("Re-enter the PIN: ");
            if (!pin.equals(again)) {
                ConsoleUtil.error("The PINs did not match, try again.");
                continue;
            }
            return pin;
        }
    }

    public static double askOpeningDeposit(AccountType type, AccountService accountService) {
        double minimum = accountService.getMinimumOpeningDeposit(type);
        while (true) {
            double amount = InputHelper.readDouble("Opening deposit (minimum "
                    + MoneyFormatter.format(minimum) + "): ");
            if (amount >= minimum && Validator.isValidAmount(amount)) {
                return amount;
            }
            ConsoleUtil.error("Opening deposit must be at least " + MoneyFormatter.format(minimum) + ".");
        }
    }
}
