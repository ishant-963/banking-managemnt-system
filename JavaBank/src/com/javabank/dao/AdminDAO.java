package com.javabank.dao;

import com.javabank.exception.DatabaseException;
import com.javabank.model.Admin;
import com.javabank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    // returns null if the username/password combination is wrong
    public Admin findByCredentials(String username, String passwordHash) throws DatabaseException {
        String sql = "SELECT admin_id, username FROM admins WHERE username = ? AND password_hash = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(rs.getInt("admin_id"), rs.getString("username"));
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Could not check admin login: " + e.getMessage(), e);
        }
    }
}
