package com.javabank.util;

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
}
