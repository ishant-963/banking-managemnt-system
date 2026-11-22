package com.javabank.dao;

import com.javabank.exception.DatabaseException;
import com.javabank.model.Customer;
import com.javabank.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CustomerDAO {

    // uses a connection passed in by the caller so it can be part of a bigger transaction
    public int insert(Connection con, Customer customer) throws DatabaseException {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("Customer was saved but no ID was returned.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // duplicate entry
                throw new DatabaseException("This phone number is already registered.", e);
            }
            throw new DatabaseException("Could not save customer: " + e.getMessage(), e);
        }
    }

    public Customer findById(int customerId) throws DatabaseException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read customer: " + e.getMessage(), e);
        }
    }

    public ArrayList<Customer> findAll() throws DatabaseException {
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        ArrayList<Customer> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Could not read customers: " + e.getMessage(), e);
        }
    }

    public void updateContact(int customerId, String phone, String email, String address) throws DatabaseException {
        String sql = "UPDATE customers SET phone = ?, email = ?, address = ? WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, email);
            ps.setString(3, address);
            ps.setInt(4, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DatabaseException("That phone number belongs to another customer.", e);
            }
            throw new DatabaseException("Could not update customer: " + e.getMessage(), e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"));
    }
}
