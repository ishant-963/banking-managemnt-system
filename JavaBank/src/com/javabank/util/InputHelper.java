package com.javabank.util;

import java.util.Scanner;

// All keyboard input goes through here so the rest of the code stays clean.
public class InputHelper {

    private static final Scanner scanner = new Scanner(System.in);

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a whole number.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid amount.");
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
            System.out.println("[ERROR] Please type y or n.");
        }
    }
}
