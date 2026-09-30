package org.example.service;

import org.example.model.Account;

import java.util.List;


public interface AccountService
{
    Account createAccount(Account account);

    Account getAccountByUsernameAndPassword(Account account);

    void recordLogin(Account account);

    boolean deposit(Account account, double amount);

    boolean withdraw(Account account, double amount);

    boolean transfer(Account from, String toUserName, double amount);

    boolean changePassword(Account account, String oldPassword, String newPassword);

    List<String> getTransactions(Account requester, String userName);

    List<Account> getAllAccounts(Account requester);

    boolean deactivateAccount(Account requester, String userName);

    boolean activateAccount(Account requester, String userName);

    boolean deleteAccount(Account requester, String userName);
}
