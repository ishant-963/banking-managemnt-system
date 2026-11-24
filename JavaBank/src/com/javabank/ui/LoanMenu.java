package com.javabank.ui;

import com.javabank.exception.BankException;
import com.javabank.model.Account;
import com.javabank.model.Loan;
import com.javabank.service.AccountService;
import com.javabank.service.LoanService;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.TablePrinter;

public class LoanMenu {

    private Account account;
    private LoanService loanService = new LoanService();
    private AccountService accountService = new AccountService();

    public LoanMenu(Account account) {
        this.account = account;
    }

    public void show() {
        boolean inMenu = true;
        while (inMenu) {
            ConsoleUtil.header("LOANS");
            System.out.println("1. Apply for a loan");
            System.out.println("2. View my loans");
            System.out.println("3. Pay an EMI");
            System.out.println("4. EMI calculator");
            System.out.println("0. Back");

            int choice = ConsoleUtil.readInt("\nEnter your choice: ");
            switch (choice) {
                case 1:
                    applyForLoan();
                    break;
                case 2:
                    viewLoans();
                    break;
                case 3:
                    payEmi();
                    break;
                case 4:
                    emiCalculator();
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    ConsoleUtil.error("Invalid choice, please try again.");
            }
        }
    }

    private void applyForLoan() {
        try {
            account = accountService.getAccount(account.getAccNo());
            System.out.println("Interest rate : " + LoanService.ANNUAL_RATE + "% per year");
            System.out.println("You can borrow: up to " + ConsoleUtil.formatMoney(loanService.getMaxLoanFor(account))
                    + " (minimum " + ConsoleUtil.formatMoney(LoanService.MIN_LOAN) + ")");

            double principal = ConsoleUtil.readDouble("Loan amount: ");
            int months = ConsoleUtil.readInt("Period in months (" + LoanService.MIN_MONTHS
                    + " to " + LoanService.MAX_MONTHS + "): ");

            double emi = loanService.calculateEmi(principal, LoanService.ANNUAL_RATE, months);
            System.out.println("Your monthly EMI would be " + ConsoleUtil.formatMoney(emi));

            if (!ConsoleUtil.confirm("Take this loan?")) {
                ConsoleUtil.info("Loan application cancelled.");
                return;
            }

            Loan loan = loanService.applyForLoan(account.getAccNo(), principal, months);
            ConsoleUtil.success("Loan #" + loan.getLoanId() + " approved. "
                    + ConsoleUtil.formatMoney(principal) + " has been added to your account.");
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void viewLoans() {
        try {
            System.out.println();
            System.out.print(TablePrinter.loanTable(loanService.getLoansOfCustomer(account.getCustomerId())));
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void payEmi() {
        int loanId = ConsoleUtil.readInt("Loan number to pay: ");
        try {
            Loan loan = loanService.payEmi(loanId, account.getAccNo());
            ConsoleUtil.success("EMI paid. Remaining on loan #" + loanId + ": "
                    + ConsoleUtil.formatMoney(loan.getRemaining()));
            if (loan.getRemaining() <= 0) {
                ConsoleUtil.success("Congratulations, this loan is now fully paid!");
            }
        } catch (BankException e) {
            ConsoleUtil.error(e.getMessage());
        }
    }

    private void emiCalculator() {
        double principal = ConsoleUtil.readDouble("Loan amount: ");
        int months = ConsoleUtil.readInt("Period in months: ");
        if (principal <= 0 || months <= 0) {
            ConsoleUtil.error("Amount and months must be greater than zero.");
            return;
        }
        double emi = loanService.calculateEmi(principal, LoanService.ANNUAL_RATE, months);
        double total = ConsoleUtil.roundMoney(emi * months);
        System.out.println("Monthly EMI    : " + ConsoleUtil.formatMoney(emi));
        System.out.println("Total payable  : " + ConsoleUtil.formatMoney(total));
        System.out.println("Total interest : " + ConsoleUtil.formatMoney(total - principal));
    }
}
