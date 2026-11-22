package com.javabank;

import com.javabank.ui.MainMenu;
import com.javabank.util.ConsoleUtil;
import com.javabank.util.DBConnection;

public class BankApp {

    public static void main(String[] args) {
        ConsoleUtil.banner();

        if (!DBConnection.testConnection()) {
            ConsoleUtil.error("Could not connect to the database.");
            System.out.println("Check that MySQL is running, schema.sql has been executed, and that the");
            System.out.println("username/password in util/DBConnection.java are correct.");
            return;
        }

        new MainMenu().start();
        System.out.println("\nThank you for banking with Java Bank. Goodbye!");
    }
}
