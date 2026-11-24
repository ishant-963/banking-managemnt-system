package com.javabank.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Validator {

    public static boolean isValidName(String name) {
        return name != null && name.matches("[A-Za-z ]{2,50}");
    }

    // 10 digit Indian mobile number
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("[6-9]\\d{9}");
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= 80
                && email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    public static boolean isValidAddress(String address) {
        return address != null && address.length() >= 5 && address.length() <= 120;
    }

    public static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    // at least one paisa, and not something silly like NaN or infinity
    public static boolean isValidAmount(double amount) {
        return amount >= 0.01 && amount < 1.0E12;
    }

    // PINs and passwords are never stored as plain text, only their SHA-256 hash.
    public static String hashPin(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available on this machine", e);
        }
    }
}
