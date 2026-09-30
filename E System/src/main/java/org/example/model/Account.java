package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Account
{
    private String userName;
    private String password;
    private Double balance = 0.0;
    private String phoneNumber;
    private Float age;

    // true only for admin accounts (the default admin is created when the application starts)
    private boolean isAdmin = false;

    // an inactive account can't login or receive money, but it is still kept in the system
    private boolean active = true;

    // history of this account, one formatted line per transaction (oldest first)
    private List<String> transactions = new ArrayList<>();

    public Account() {
    }

    public Account(String userName, String password, String phoneNumber, Float age) {
        this.userName = userName;
        this.password = password;
        this.balance = 0.0;
        this.phoneNumber = phoneNumber;
        this.age = age;
    }

    public Account(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Float getAge() {
        return age;
    }

    public void setAge(Float age) {
        this.age = age;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }
}
