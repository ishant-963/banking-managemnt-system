package com.javabank.model;

public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private String email;
    private String address;

    // used when a brand new customer registers (id comes from the database later)
    public Customer(String name, String phone, String email, String address) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // used when a customer is read back from the database
    public Customer(int customerId, String name, String phone, String email, String address) {
        this(name, phone, email, address);
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
