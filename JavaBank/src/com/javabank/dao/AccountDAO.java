package com.javabank.dao;

import com.javabank.exception.DatabaseException;
import com.javabank.model.Account;
import com.javabank.model.AccountStatus;
import com.javabank.model.AccountType;
import com.javabank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AccountDAO {

    public int insert(Connection con, int customerId, AccountType type, String pinHash, double balance)
            throws DatabaseException {
        String sql = "INSERT INTO accounts (customer_id, acc_type, balance, pin_hash, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setString(2, type.name());
            ps.setDouble(3, balance);
            ps.setString(4, pinHash);
            ps.setString(5, AccountStatus.ACTIVE.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("Account was created but no account number was returned.");
        } catch (SQLException e) {
            throw new DatabaseException("Could not create account: " + e.getMessage(), e);
        }
    }

    // returns null when there is no such account
    public Account findByAccNo(int accNo) throws DatabaseException {
        String sql = "SELECT * FROM accounts WHERE acc_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read account: " + e.getMessage(), e);
        }
    }

    public ArrayList<Account> findByCustomer(int customerId) throws DatabaseException {
        ArrayList<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY acc_no";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read accounts: " + e.getMessage(), e);
        }
    }

    public ArrayList<Account> findAll() throws DatabaseException {
        ArrayList<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY acc_no";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read accounts: " + e.getMessage(), e);
        }
    }

    public ArrayList<Account> findByType(AccountType type) throws DatabaseException {
        ArrayList<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE acc_type = ? ORDER BY acc_no";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read accounts: " + e.getMessage(), e);
        }
    }

    public String getPinHash(int accNo) throws DatabaseException {
        String sql = "SELECT pin_hash FROM accounts WHERE acc_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("pin_hash");
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read PIN: " + e.getMessage(), e);
        }
    }

    public void updateBalance(Connection con, int accNo, double newBalance) throws DatabaseException {
        String sql = "UPDATE accounts SET balance = ? WHERE acc_no = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update balance: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int accNo, AccountStatus status) throws DatabaseException {
        String sql = "UPDATE accounts SET status = ? WHERE acc_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, accNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update account status: " + e.getMessage(), e);
        }
    }

    public void updatePin(int accNo, String newPinHash) throws DatabaseException {
        String sql = "UPDATE accounts SET pin_hash = ? WHERE acc_no = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPinHash);
            ps.setInt(2, accNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not change PIN: " + e.getMessage(), e);
        }
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("acc_no"),
                rs.getInt("customer_id"),
                AccountType.valueOf(rs.getString("acc_type")),
                rs.getDouble("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("opened_on"));
    }
}
