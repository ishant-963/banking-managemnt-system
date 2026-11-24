package com.javabank.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Scanner;

// Console output, keyboard input, and money/date formatting helpers.
public class ConsoleUtil {

    private static final int WIDTH = 60;
    private static final Scanner scanner = new Scanner(System.in);

    public static void banner() {
        System.out.println("=".repeat(WIDTH));
        System.out.println("             J A V A   B A N K");
        System.out.println("        Banking Management System (Terminal)");
        System.out.println("=".repeat(WIDTH));
    }

    public static void header(String title) {
        System.out.println();
        System.out.println("=".repeat(WIDTH));
        int padding = Math.max(0, (WIDTH - title.length()) / 2);
        System.out.println(" ".repeat(padding) + title);
        System.out.println("=".repeat(WIDTH));
    }

    public static void line() {
        System.out.println("-".repeat(WIDTH));
    }

    public static void success(String message) {
        System.out.println("[OK] " + message);
    }

    public static void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void pause() {
        readLine("\nPress Enter to continue...");
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                error("Please enter a whole number.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            try {
                return Double.parseDouble(readLine(prompt));
            } catch (NumberFormatException e) {
                error("Please enter a valid amount.");
            }
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            String answer = readLine(prompt + " (y/n): ").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            error("Please type y or n.");
        }
    }

    public static String formatMoney(double amount) {
        return String.format(Locale.US, "Rs. %,.2f", amount);
    }

    // keeps money values at 2 decimal places
    public static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    public static String formatDate(Timestamp time) {
        if (time == null) {
            return "-";
        }
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(time);
    }
}
