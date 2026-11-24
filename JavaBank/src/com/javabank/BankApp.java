package com.javabank;

import com.javabank.ui.MainMenu;
import com.javabank.util.ConsoleUtil;//custom static method, so thats why no object creation
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
        System.out.println("\nThank you for banking with our Bank. Goodbye!");
    }
}

