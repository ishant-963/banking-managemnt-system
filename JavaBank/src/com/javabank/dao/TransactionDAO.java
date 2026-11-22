package com.javabank.dao;

import com.javabank.exception.DatabaseException;
import com.javabank.model.Transaction;
import com.javabank.model.TransactionType;
import com.javabank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TransactionDAO {

    public void insert(Connection con, Transaction t) throws DatabaseException {
        String sql = "INSERT INTO transactions (acc_no, txn_type, amount, balance_after, description) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, t.getAccNo());
            ps.setString(2, t.getType().name());
            ps.setDouble(3, t.getAmount());
            ps.setDouble(4, t.getBalanceAfter());
            ps.setString(5, t.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not record transaction: " + e.getMessage(), e);
        }
    }

    // newest first, only the latest `limit` rows
    public ArrayList<Transaction> findLatestByAccount(int accNo, int limit) throws DatabaseException {
        String sql = "SELECT * FROM transactions WHERE acc_no = ? ORDER BY txn_id DESC LIMIT ?";
        ArrayList<Transaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read transactions: " + e.getMessage(), e);
        }
    }

    // oldest first, used for full statements
    public ArrayList<Transaction> findAllByAccount(int accNo) throws DatabaseException {
        String sql = "SELECT * FROM transactions WHERE acc_no = ? ORDER BY txn_id ASC";
        ArrayList<Transaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read transactions: " + e.getMessage(), e);
        }
    }

    // latest transactions across the whole bank (admin view)
    public ArrayList<Transaction> findRecent(int limit) throws DatabaseException {
        String sql = "SELECT * FROM transactions ORDER BY txn_id DESC LIMIT ?";
        ArrayList<Transaction> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read transactions: " + e.getMessage(), e);
        }
    }

    // stops the admin from crediting interest twice in the same month
    public boolean hasInterestThisMonth(int accNo) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE acc_no = ? AND txn_type = 'INTEREST' "
                + "AND YEAR(txn_time) = YEAR(CURDATE()) AND MONTH(txn_time) = MONTH(CURDATE())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check interest history: " + e.getMessage(), e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("txn_id"),
                rs.getInt("acc_no"),
                TransactionType.valueOf(rs.getString("txn_type")),
                rs.getDouble("amount"),
                rs.getDouble("balance_after"),
                rs.getString("description"),
                rs.getTimestamp("txn_time"));
    }
}
