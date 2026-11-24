package com.javabank.dao;

import com.javabank.exception.DatabaseException;
import com.javabank.model.Loan;
import com.javabank.model.LoanStatus;
import com.javabank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LoanDAO {

    public int insert(Connection con, Loan loan) throws DatabaseException {
        String sql = "INSERT INTO loans (customer_id, acc_no, principal, annual_rate, months, emi, remaining, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getCustomerId());
            ps.setInt(2, loan.getAccNo());
            ps.setDouble(3, loan.getPrincipal());
            ps.setDouble(4, loan.getAnnualRate());
            ps.setInt(5, loan.getMonths());
            ps.setDouble(6, loan.getEmi());
            ps.setDouble(7, loan.getRemaining());
            ps.setString(8, loan.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("Loan was saved but no loan ID was returned.");
        } catch (SQLException e) {
            throw new DatabaseException("Could not save loan: " + e.getMessage(), e);
        }
    }

    public Loan findById(int loanId) throws DatabaseException {
        ArrayList<Loan> results = runListQuery("SELECT * FROM loans WHERE loan_id = ?", loanId);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public ArrayList<Loan> findByCustomer(int customerId) throws DatabaseException {
        return runListQuery("SELECT * FROM loans WHERE customer_id = ? ORDER BY loan_id", customerId);
    }

    public ArrayList<Loan> findAll() throws DatabaseException {
        return runListQuery("SELECT * FROM loans ORDER BY loan_id", null);
    }

    private ArrayList<Loan> runListQuery(String sql, Integer param) throws DatabaseException {
        ArrayList<Loan> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read loans: " + e.getMessage(), e);
        }
    }

    public void updateRemaining(Connection con, int loanId, double remaining, LoanStatus status)
            throws DatabaseException {
        String sql = "UPDATE loans SET remaining = ?, status = ? WHERE loan_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, remaining);
            ps.setString(2, status.name());
            ps.setInt(3, loanId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Could not update loan: " + e.getMessage(), e);
        }
    }

    private Loan mapRow(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getInt("loan_id"),
                rs.getInt("customer_id"),
                rs.getInt("acc_no"),
                rs.getDouble("principal"),
                rs.getDouble("annual_rate"),
                rs.getInt("months"),
                rs.getDouble("emi"),
                rs.getDouble("remaining"),
                LoanStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("taken_on"));
    }
}
