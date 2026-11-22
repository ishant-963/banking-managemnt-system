package com.javabank.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MoneyFormatter {

    public static String format(double amount) {
        return String.format(Locale.US, "Rs. %,.2f", amount);
    }

    // keeps money values at 2 decimal places
    public static double round(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    public static String date(Timestamp time) {
        if (time == null) {
            return "-";
        }
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(time);
    }
}
