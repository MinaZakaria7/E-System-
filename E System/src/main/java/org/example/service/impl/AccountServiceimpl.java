package org.example.service.impl;

import org.example.model.Account;
import org.example.model.WalletSystem;
import org.example.service.AccountService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class AccountServiceimpl implements AccountService

{

    private static final String ADMIN_USERNAME = "IAM";
    private static final String ADMIN_PASSWORD = "IAM123";

    // transaction types written in the account history
    private static final String SIGNUP = "SIGNUP";
    private static final String LOGIN = "LOGIN";
    private static final String DEPOSIT = "DEPOSIT";
    private static final String WITHDRAW = "WITHDRAW";
    private static final String TRANSFER_OUT = "TRANSFER_OUT";
    private static final String TRANSFER_IN = "TRANSFER_IN";
    private static final String DEACTIVATE = "DEACTIVATE";
    private static final String ACTIVATE = "ACTIVATE";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // balances are doubles, so a "zero" balance can be a tiny leftover like 5.5E-17
    private static final double ZERO_TOLERANCE = 0.005;

    private WalletSystem walletSystem = new WalletSystem();

    /**
     * Creates the service and adds the default admin account
     * (username IAM / password IAM123) every time the application starts.
     */
    public AccountServiceimpl() {
        createDefaultAdmin();
    }

    @Override
    public Account createAccount(Account account) {
        if (Objects.isNull(account) || isBlank(account.getUserName()) || isBlank(account.getPassword())) {
            return null;
        }

        boolean isAccountExistWithSameUserName = walletSystem.getAccounts().stream()
                .anyMatch(acc -> acc.getUserName().equals(account.getUserName()));

        if (isAccountExistWithSameUserName) {
            return null;
        }

        walletSystem.getAccounts().add(account);
        record(account, SIGNUP, "account created");
        return account;
    }

    @Override
    public Account getAccountByUsernameAndPassword(Account account) {
        if (Objects.isNull(account) || isBlank(account.getUserName()) || isBlank(account.getPassword())) {
            return null;
        }

        Optional<Account> existedAccount = walletSystem.getAccounts().stream()
                .filter(acc -> acc.getUserName().equals(account.getUserName()) &&
                        acc.getPassword().equals(account.getPassword())).findFirst();

        if (existedAccount.isPresent()) {
            return existedAccount.get();
        }

        return null;
    }

    /**
     * Writes a LOGIN line in the history of the account.
     * Nothing is written for an inactive account because it is not allowed to login.
     */
    @Override
    public void recordLogin(Account account) {
        if (Objects.isNull(account) || !account.isActive()) {
            return;
        }

        record(account, LOGIN, "logged in");
    }

    /**
     * Adds money to the balance of the given account.
     *
     * @return true if the deposit succeeded, false if the amount is invalid
     *         or the account is inactive
     */
    @Override
    public boolean deposit(Account account, double amount) {
        if (Objects.isNull(account) || !account.isActive() || !isValidAmount(amount)) {
            return false;
        }

        account.setBalance(account.getBalance() + amount);
        record(account, DEPOSIT, "+" + amount);
        return true;
    }

    /**
     * Takes money from the balance of the given account.
     *
     * @return true if the withdraw succeeded, false if the amount is invalid,
     *         bigger than the current balance, or the account is inactive
     */
    @Override
    public boolean withdraw(Account account, double amount) {
        if (Objects.isNull(account) || !account.isActive()
                || !isValidAmount(amount) || amount > account.getBalance()) {
            return false;
        }

        account.setBalance(account.getBalance() - amount);
        record(account, WITHDRAW, "-" + amount);
        return true;
    }

    /**
     * Moves money from one account to another account (found by username).
     * A TRANSFER_OUT line is written for the sender and a TRANSFER_IN line
     * for the receiver.
     *
     * @return true if the transfer succeeded, false if the receiver does not
     *         exist or is inactive, the receiver is the sender himself, the
     *         amount is invalid, or the sender's balance is not enough
     */
    @Override
    public boolean transfer(Account from, String toUserName, double amount) {
        if (Objects.isNull(from) || !from.isActive() || isBlank(toUserName)) {
            return false;
        }

        if (from.getUserName().equals(toUserName)) {
            return false;
        }

        Account to = findByUserName(toUserName);

        if (to == null || !to.isActive()) {
            return false;
        }

        if (!isValidAmount(amount) || amount > from.getBalance()) {
            return false;
        }

        // the balances are changed here (not through withdraw/deposit)
        // so the history shows one transfer, not a withdraw plus a deposit
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        record(from, TRANSFER_OUT, "-" + amount + " to " + to.getUserName());
        record(to, TRANSFER_IN, "+" + amount + " from " + from.getUserName());
        return true;
    }

    /**
     * Changes the password of the given account.
     *
     * @return true if the old password is correct and the new one is not empty
     */
    @Override
    public boolean changePassword(Account account, String oldPassword, String newPassword) {
        if (Objects.isNull(account) || isBlank(oldPassword) || isBlank(newPassword)) {
            return false;
        }

        if (!account.getPassword().equals(oldPassword)) {
            return false;
        }

        account.setPassword(newPassword);
        return true;
    }

    /**
     * Returns the transaction history of an account, oldest first.
     * A normal user can read only his own history, an admin can read any history.
     *
     * @return the history lines, or null if the account does not exist
     *         or the requester is not allowed to see it
     */
    @Override
    public List<String> getTransactions(Account requester, String userName) {
        if (Objects.isNull(requester) || !requester.isActive() || isBlank(userName)) {
            return null;
        }

        if (!requester.isAdmin() && !requester.getUserName().equals(userName)) {
            return null;
        }

        Account target = findByUserName(userName);

        if (target == null) {
            return null;
        }

        return new ArrayList<>(target.getTransactions());
    }

    /**
     * Returns all the accounts in the system (admin only).
     *
     * @return a copy of the accounts list, or an empty list if the requester is not an admin
     */
    @Override
    public List<Account> getAllAccounts(Account requester) {
        if (Objects.isNull(requester) || !requester.isActive() || !requester.isAdmin()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(walletSystem.getAccounts());
    }

    /**
     * Makes an account inactive: it can no longer login, send or receive money.
     *
     * @return true if the account became inactive, false if it is not allowed
     *         (see {@link #findManageableAccount}), or it was already inactive
     */
    @Override
    public boolean deactivateAccount(Account requester, String userName) {
        Account target = findManageableAccount(requester, userName);

        if (target == null || !target.isActive()) {
            return false;
        }

        target.setActive(false);
        record(target, DEACTIVATE, "by " + requester.getUserName());
        return true;
    }

    /**
     * Makes an inactive account active again. Only an admin can do this,
     * because the owner of an inactive account can't login.
     *
     * @return true if the account became active, false if it is not allowed
     *         or it was already active
     */
    @Override
    public boolean activateAccount(Account requester, String userName) {
        Account target = findManageableAccount(requester, userName);

        if (target == null || target.isActive()) {
            return false;
        }

        target.setActive(true);
        record(target, ACTIVATE, "by " + requester.getUserName());
        return true;
    }

    /**
     * Deletes an account (and its history) from the system.
     * The balance must be zero, so no money disappears with the account.
     *
     * @return true if the account was deleted, false if it is not allowed
     *         (see {@link #findManageableAccount}) or it still has money
     */
    @Override
    public boolean deleteAccount(Account requester, String userName) {
        Account target = findManageableAccount(requester, userName);

        if (target == null || Math.abs(target.getBalance()) > ZERO_TOLERANCE) {
            return false;
        }

        return walletSystem.getAccounts().remove(target);
    }

    /**
     * Finds the account that the requester is allowed to deactivate, activate or delete.
     *
     * <p>A normal user can manage only his own account, an admin can manage any account.
     * Admin accounts can never be managed, so the system always keeps an admin.</p>
     *
     * @return the account, or null if it does not exist or the requester is not allowed
     */
    private Account findManageableAccount(Account requester, String userName) {
        if (Objects.isNull(requester) || !requester.isActive() || isBlank(userName)) {
            return null;
        }

        if (!requester.isAdmin() && !requester.getUserName().equals(userName)) {
            return null;
        }

        Account target = findByUserName(userName);

        if (target == null || target.isAdmin()) {
            return null;
        }

        return target;
    }

    private void createDefaultAdmin() {
        if (findByUserName(ADMIN_USERNAME) != null) {
            return;
        }

        Account admin = new Account(ADMIN_USERNAME, ADMIN_PASSWORD, "N/A", 0f);
        admin.setAdmin(true);
        createAccount(admin);
    }

    // adds one line to the history: time | type | details | balance after the operation
    private void record(Account account, String type, String details) {
        account.getTransactions().add(
                LocalDateTime.now().format(TIME_FORMAT) + " | " + type + " | " + details
                        + " | balance: " + account.getBalance());
    }

    private Account findByUserName(String userName) {
        if (isBlank(userName)) {
            return null;
        }

        return walletSystem.getAccounts().stream()
                .filter(acc -> acc.getUserName().equals(userName))
                .findFirst()
                .orElse(null);
    }

    // NaN fails "amount > 0", so it is rejected here too
    private boolean isValidAmount(double amount) {
        return amount > 0 && !Double.isInfinite(amount);
    }

    // centralized null/empty/whitespace-only check, used before any value
    // is allowed to move on to the next step (comparisons, storage, ...)
    private boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }

}
