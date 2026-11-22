package com.javabank.util;

public class ConsoleUtil {

    private static final int WIDTH = 60;

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
        InputHelper.readLine("\nPress Enter to continue...");
    }
}
