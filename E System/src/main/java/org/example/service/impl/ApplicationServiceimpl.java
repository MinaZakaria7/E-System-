package org.example.service.impl;

import org.example.model.Account;
import org.example.model.WalletSystem;
import org.example.service.AccountService;
import org.example.service.ApplicationService;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ApplicationServiceimpl implements ApplicationService {
    private Scanner scanner = new Scanner(System.in);

    private AccountService accountService = new AccountServiceimpl();

    /**
     * Starts the wallet application.
     *
     * <p>Displays the main menu and allows the user to choose between
     * login, signup, and exit operations.</p>
     *
     * <p>The application allows up to four invalid menu selections.
     * After reaching the limit, the application terminates and asks
     * the user to contact the administrator.</p>
     */
    @Override
    public void start() {
        System.out.println("-> Welcome to " + WalletSystem.name);

        Integer count = 0;

        while (true) {

            System.out.println("please choose......");
            System.out.println("1.login    2.signup     3.Exit");

            int choose = readInt();

            Boolean isExit = false;

            switch (choose) {

                case 1:
                    login();
                    break;

                case 2:
                    signup();
                    break;

                case 3:
                    System.out.println("have a nice day :)");
                    isExit = true;
                    break;

                default:
                    System.out.println("invalid choose :(");
                    count++;
            }

            if (isExit) {
                break;
            }

            if (count == 4) {
                System.out.println("pls contact with Admin :(");
                break;
            }
        }
    }

    /**
     * Handles the user login process.
     *
     * <p>The user provides a username and password. The credentials are
     * passed to {@link AccountService} to find an existing account.</p>
     *
     * <p>If the account exists and is active, the user is redirected to the
     * main wallet profile (or to the admin panel if the account is an admin).
     * If the account is inactive, or does not exist, an error message is displayed.</p>
     */
    private void login() {

        String userName = readNonBlank("Please enter your username: ");
        String password = readNonBlank("Please enter your password: ");

        Account account = new Account(userName, password);

        Account existedAccount =
                accountService.getAccountByUsernameAndPassword(account);

        if (Objects.isNull(existedAccount)) {

            System.out.println(
                    "account not exist pls check username and password :("
            );

        } else if (!existedAccount.isActive()) {

            System.out.println("this account is inactive, pls contact with Admin :(");

        } else {

            accountService.recordLogin(existedAccount);

            System.out.println("success login :)");

            if (existedAccount.isAdmin()) {
                adminPanel(existedAccount);
            } else {
                mainProfile(existedAccount);
            }
        }
    }

    /**
     * Handles the new account registration process.
     *
     * <p>The user provides the required account information including
     * username, password, phone number, and age.</p>
     *
     * <p>The account is created through {@link AccountService}. If the
     * username already exists, account creation fails.</p>
     */
    private void signup() {

        String userName = readNonBlank("Please enter your username: ");
        String password = readNonBlank("Please enter your password: ");
        String phoneNumber = readNonBlank("Please enter your phone number: ");
        Float age = readAge("Please enter your age: ");

        Account accountCreate =
                new Account(userName, password, phoneNumber, age);

        // Create account
        accountCreate = accountService.createAccount(accountCreate);

        if (Objects.isNull(accountCreate)) {

            System.out.println(
                    "account failed to create because username already exist...."
            );

        } else {

            System.out.println("account created success...");

            mainProfile(accountCreate);
        }
    }

    /**
     * Displays the main wallet profile menu for the authenticated user.
     *
     * <p>After successful login or signup, the user can perform wallet
     * operations such as depositing money, withdrawing money, transferring
     * money, viewing the balance, viewing account details, changing the
     * password, viewing the transaction history, deactivating or deleting
     * his own account, or logging out.</p>
     *
     * <p>The menu keeps showing until the user chooses logout.</p>
     *
     * @param account the currently authenticated user's account
     */
    private void mainProfile(Account account) {

        while (true) {

            System.out.println(
                    "1.deposit   " +
                            "2.withdraw  " +
                            "3.Transfer   " +
                            "4.show balance    " +
                            "5.show details   " +
                            "6.Change Password"
            );
            System.out.println(
                    "7.transaction history   " +
                            "8.deactivate my account   " +
                            "9.delete my account   " +
                            "10.logout"
            );

            int choose = readInt();

            switch (choose) {

                case 1:
                    deposit(account);
                    break;

                case 2:
                    withdraw(account);
                    break;

                case 3:
                    transfer(account);
                    break;

                case 4:
                    showBalance(account);
                    break;

                case 5:
                    showDetails(account);
                    break;

                case 6:
                    changePassword(account);
                    break;

                case 7:
                    showTransactions(account, account.getUserName());
                    break;

                case 8:
                    if (deactivateOwnAccount(account)) {
                        return;
                    }
                    break;

                case 9:
                    if (deleteOwnAccount(account)) {
                        return;
                    }
                    break;

                case 10:
                    System.out.println("logged out :)");
                    return;

                default:
                    System.out.println("invalid choose :(");
            }
        }
    }

    /**
     * Asks the user for an amount and deposits it into his account.
     *
     * @param account the currently authenticated user's account
     */
    private void deposit(Account account) {

        Double amount = readAmount("Please enter the amount to deposit: ");

        if (Objects.isNull(amount)) {
            return;
        }

        if (accountService.deposit(account, amount)) {
            System.out.println("deposit success, your balance is: " + account.getBalance());
        } else {
            System.out.println("deposit failed, the amount must be greater than 0 :(");
        }
    }

    /**
     * Asks the user for an amount and withdraws it from his account.
     *
     * @param account the currently authenticated user's account
     */
    private void withdraw(Account account) {

        Double amount = readAmount("Please enter the amount to withdraw: ");

        if (Objects.isNull(amount)) {
            return;
        }

        if (accountService.withdraw(account, amount)) {
            System.out.println("withdraw success, your balance is: " + account.getBalance());
        } else {
            System.out.println("withdraw failed, the amount must be greater than 0 and not more than your balance :(");
        }
    }

    /**
     * Asks the user for the receiver's username and an amount,
     * then transfers the money to the receiver.
     *
     * @param account the currently authenticated user's account
     */
    private void transfer(Account account) {

        String toUserName = readNonBlank("Please enter the receiver username: ");

        Double amount = readAmount("Please enter the amount to transfer: ");

        if (Objects.isNull(amount)) {
            return;
        }

        if (accountService.transfer(account, toUserName, amount)) {
            System.out.println("transfer success, your balance is: " + account.getBalance());
        } else {
            System.out.println("transfer failed, check that the receiver exists (and is not you), " +
                    "and that the amount is greater than 0 and not more than your balance :(");
        }
    }

    /**
     * Prints the current balance of the user.
     *
     * @param account the currently authenticated user's account
     */
    private void showBalance(Account account) {

        System.out.println("your balance is: " + account.getBalance());
    }

    /**
     * Prints the account details of the user (the password is not shown).
     *
     * @param account the currently authenticated user's account
     */
    private void showDetails(Account account) {

        System.out.println("username : " + account.getUserName());
        System.out.println("phone    : " + account.getPhoneNumber());
        System.out.println("age      : " + account.getAge());
        System.out.println("balance  : " + account.getBalance());
        System.out.println("role     : " + roleOf(account));
        System.out.println("status   : " + statusOf(account));
    }

    /**
     * Asks the user for the old and the new password and changes it.
     *
     * @param account the currently authenticated user's account
     */
    private void changePassword(Account account) {

        String oldPassword = readNonBlank("Please enter your old password: ");
        String newPassword = readNonBlank("Please enter your new password: ");

        if (accountService.changePassword(account, oldPassword, newPassword)) {
            System.out.println("password changed success :)");
        } else {
            System.out.println("password change failed, the old password is wrong :(");
        }
    }

    /**
     * Prints the transaction history of an account, oldest first.
     *
     * @param requester the currently authenticated account (user or admin)
     * @param userName  the owner of the history to print
     */
    private void showTransactions(Account requester, String userName) {

        List<String> transactions = accountService.getTransactions(requester, userName);

        if (Objects.isNull(transactions)) {
            System.out.println("can't show the history, the account does not exist :(");
            return;
        }

        System.out.println("---- transaction history of " + userName + " ----");

        if (transactions.isEmpty()) {
            System.out.println("no transactions yet");
        }

        for (String transaction : transactions) {
            System.out.println(transaction);
        }
    }

    /**
     * Deactivates the account of the logged in user after he confirms with his password.
     *
     * @param account the currently authenticated user's account
     * @return true if the account became inactive (the user is logged out), false otherwise
     */
    private boolean deactivateOwnAccount(Account account) {

        if (!confirmWithPassword(account, "deactivate")) {
            return false;
        }

        if (accountService.deactivateAccount(account, account.getUserName())) {
            System.out.println("your account is now inactive, you are logged out. " +
                    "contact with Admin to activate it again :)");
            return true;
        }

        System.out.println("deactivate failed, admin accounts can't be deactivated :(");
        return false;
    }

    /**
     * Deletes the account of the logged in user after he confirms with his password.
     *
     * @param account the currently authenticated user's account
     * @return true if the account was deleted (the user is logged out), false otherwise
     */
    private boolean deleteOwnAccount(Account account) {

        if (!confirmWithPassword(account, "delete")) {
            return false;
        }

        if (accountService.deleteAccount(account, account.getUserName())) {
            System.out.println("your account was deleted, you are logged out :)");
            return true;
        }

        System.out.println("delete failed, admin accounts can't be deleted, " +
                "and your balance must be 0 (withdraw or transfer your money first) :(");
        return false;
    }

    /**
     * Asks the user for his password before a dangerous operation.
     *
     * @param account   the currently authenticated user's account
     * @param operation the name of the operation, used in the messages
     * @return true if the password is correct
     */
    private boolean confirmWithPassword(Account account, String operation) {

        String password = readNonBlank("Please enter your password to " + operation + " your account: ");

        if (Objects.isNull(accountService.getAccountByUsernameAndPassword(
                new Account(account.getUserName(), password)))) {
            System.out.println("wrong password, " + operation + " cancelled :(");
            return false;
        }

        return true;
    }

    /**
     * Displays the admin panel for the authenticated admin.
     *
     * <p>The admin can view all the accounts, view the history of any account,
     * activate, deactivate or delete accounts, open his own wallet, or logout.</p>
     *
     * @param admin the currently authenticated admin account
     */
    private void adminPanel(Account admin) {

        while (true) {

            System.out.println("---- Admin panel ----");
            System.out.println(
                    "1.view all accounts   " +
                            "2.view account history   " +
                            "3.deactivate account   " +
                            "4.activate account"
            );
            System.out.println(
                    "5.delete account   " +
                            "6.my wallet   " +
                            "7.logout"
            );

            int choose = readInt();

            switch (choose) {

                case 1:
                    showAllAccounts(admin);
                    break;

                case 2:
                    showTransactions(admin, readNonBlank("Please enter the account username: "));
                    break;

                case 3:
                    adminDeactivate(admin);
                    break;

                case 4:
                    adminActivate(admin);
                    break;

                case 5:
                    adminDelete(admin);
                    break;

                case 6:
                    mainProfile(admin);
                    break;

                case 7:
                    System.out.println("logged out :)");
                    return;

                default:
                    System.out.println("invalid choose :(");
            }
        }
    }

    /**
     * Prints a table with all the accounts (the passwords are not shown).
     *
     * @param admin the currently authenticated admin account
     */
    private void showAllAccounts(Account admin) {

        List<Account> accounts = accountService.getAllAccounts(admin);

        System.out.println("---- All accounts (" + accounts.size() + ") ----");
        System.out.printf("%-15s %-15s %-6s %-12s %-7s %-8s%n",
                "USERNAME", "PHONE", "AGE", "BALANCE", "ROLE", "STATUS");

        for (Account acc : accounts) {
            System.out.printf("%-15s %-15s %-6s %-12s %-7s %-8s%n",
                    acc.getUserName(),
                    acc.getPhoneNumber(),
                    acc.getAge(),
                    acc.getBalance(),
                    roleOf(acc),
                    statusOf(acc));
        }
    }

    private void adminDeactivate(Account admin) {

        String userName = readNonBlank("Please enter the username to deactivate: ");

        if (accountService.deactivateAccount(admin, userName)) {
            System.out.println("account " + userName + " is now inactive :)");
        } else {
            System.out.println("deactivate failed, check that the account exists, " +
                    "is active, and is not an admin account :(");
        }
    }

    private void adminActivate(Account admin) {

        String userName = readNonBlank("Please enter the username to activate: ");

        if (accountService.activateAccount(admin, userName)) {
            System.out.println("account " + userName + " is now active :)");
        } else {
            System.out.println("activate failed, check that the account exists " +
                    "and is currently inactive :(");
        }
    }

    private void adminDelete(Account admin) {

        String userName = readNonBlank("Please enter the username to delete: ");
        String confirm = readNonBlank("Type YES to delete " + userName + " forever: ");

        if (!confirm.equalsIgnoreCase("YES")) {
            System.out.println("delete cancelled :(");
            return;
        }

        if (accountService.deleteAccount(admin, userName)) {
            System.out.println("account " + userName + " was deleted :)");
        } else {
            System.out.println("delete failed, check that the account exists, " +
                    "is not an admin account, and its balance is 0 :(");
        }
    }

    private String roleOf(Account account) {
        return account.isAdmin() ? "ADMIN" : "USER";
    }

    private String statusOf(Account account) {
        return account.isActive() ? "ACTIVE" : "INACTIVE";
    }

    /**
     * Keeps asking the user for a value until he enters something that is
     * not null, not empty, and not just whitespace.
     *
     * <p>This is the single place that guarantees a required text field
     * (username, password, phone number, ...) is valid before it is
     * passed on to the account service.</p>
     *
     * @param message the text shown to the user before reading
     * @return the trimmed, non-blank value the user entered
     */
    private String readNonBlank(String message) {

        while (true) {

            System.out.print(message);
            String input = scanner.next().trim();

            if (!input.isBlank()) {
                return input;
            }

            System.out.println("this field can't be empty, please try again :(");
        }
    }

    /**
     * Keeps asking the user for an age until he enters a realistic,
     * positive number.
     *
     * @param message the text shown to the user before reading
     * @return a valid age between 1 and 120
     */
    private Float readAge(String message) {

        while (true) {

            System.out.print(message);
            String input = scanner.next();

            try {
                float age = Float.parseFloat(input);

                if (age <= 0 || age > 120) {
                    System.out.println("please enter a realistic age (1-120) :(");
                    continue;
                }

                return age;

            } catch (NumberFormatException e) {
                System.out.println("invalid age, please enter a number :(");
            }
        }
    }

    /**
     * Reads a menu choice from the user.
     *
     * @return the number the user typed, or -1 if he typed something that is not a number
     */
    private int readInt() {

        String input = scanner.next();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Reads an amount of money from the user.
     *
     * @param message the text shown to the user before reading
     * @return the amount, or null (after printing an error) if the input is not a valid number
     */
    private Double readAmount(String message) {

        System.out.print(message);
        String input = scanner.next();

        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("invalid amount, please enter a number :(");
            return null;
        }
    }
}
