package org.example.model;
import java.util.ArrayList;
import java.util.List;

public class WalletSystem
{
    public final static String name = "EraaSoft Wallet";
    private List<Account> accounts = new ArrayList<>();

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}