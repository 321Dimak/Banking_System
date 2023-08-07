package main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static JdbcConnector connector = new JdbcConnector();

    public static void main(String[] args) {
        deleteAllAccounts();
        BankAccount[] accounts = new BankAccount[10];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = createAccount(1000.0);
        }

        // Fixed thread pool executor to manage the threads
        ExecutorService executor = Executors.newFixedThreadPool(accounts.length);

        for (int i = 0; i < 5; i++) {
            final BankAccount fromAccount = accounts[i];
            final BankAccount toAccount = accounts[(i + 1) % accounts.length]; // wrap around to the first account
            executor.submit(() -> {
                fromAccount.transfer(toAccount, 100.0);
                updateAccountBalance(fromAccount.getId(), fromAccount.getBalance());
                updateAccountBalance(toAccount.getId(), toAccount.getBalance());
            });
        }

        // Shutdown the executor and wait for all tasks to complete
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        for (BankAccount account : accounts) {
            account.getBalance();
        }
    }

    static BankAccount createAccount(double balance) {
        BankAccount bankAccount = new BankAccount(balance);
        saveAccount(bankAccount);
        return bankAccount;
    }

    static void saveAccount(BankAccount account) {
        String insertQuery = "INSERT INTO account (id, balance) VALUES (?, ?)";
        try (Connection connection = connector.connect();
             PreparedStatement ps = connection.prepareStatement(insertQuery)) {

            ps.setInt(1, account.getId());
            ps.setDouble(2, account.getBalance());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void deleteAllAccounts() {
        String deleteQuery = "DELETE FROM account";
        try (Connection connection = connector.connect();
             PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void updateAccountBalance(int accountId, double newBalance) {
        String updateQuery = "UPDATE account SET balance = ? WHERE id = ?";
        try (Connection connection = connector.connect();
             PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
