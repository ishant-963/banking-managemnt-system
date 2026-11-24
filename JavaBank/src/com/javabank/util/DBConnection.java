package com.javabank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/javabank_db"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "@Is80913140779";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // called once at startup so we can show a friendly message if MySQL is not reachable
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null;
        } catch (SQLException e) {
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }

    public static void rollbackQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.rollback();
        } catch (SQLException e) {
            System.out.println("Warning: rollback failed - " + e.getMessage());
        }
    }

    public static void closeQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.close();
        } catch (SQLException e) {
            // nothing useful we can do here
        }
    }
}











