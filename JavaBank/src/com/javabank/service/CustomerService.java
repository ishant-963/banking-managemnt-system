package com.javabank.service;

import com.javabank.dao.CustomerDAO;
import com.javabank.exception.BankException;
import com.javabank.model.Customer;
import com.javabank.util.Validator;

import java.util.ArrayList;

public class CustomerService {

    private CustomerDAO customerDAO = new CustomerDAO();

    public Customer getCustomer(int customerId) throws BankException {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new BankException("Customer " + customerId + " was not found.");
        }
        return customer;
    }

    public ArrayList<Customer> getAllCustomers() throws BankException {
        return customerDAO.findAll();
    }

    public void updateContact(int customerId, String phone, String email, String address) throws BankException {
        if (!Validator.isValidPhone(phone)) {
            throw new BankException("Phone must be a valid 10 digit mobile number.");
        }
        if (!Validator.isValidEmail(email)) {
            throw new BankException("That email address does not look right.");
        }
        if (!Validator.isValidAddress(address)) {
            throw new BankException("Address must be between 5 and 120 characters.");
        }
        customerDAO.updateContact(customerId, phone, email, address);
    }
}
