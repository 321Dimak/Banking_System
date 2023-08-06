package main;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        BankAccount[] accounts = new BankAccount[10];

        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new BankAccount(1000.0);
        }

        // Fixed thread pool executor to manage the threads
        ExecutorService executor = Executors.newFixedThreadPool(accounts.length);

        for (int i = 0; i < accounts.length; i++) {
            final BankAccount fromAccount = accounts[i];
            final BankAccount toAccount = accounts[(i+1) % accounts.length]; // wrap around to the first account
            executor.submit(() -> {
                fromAccount.transfer(toAccount, 100.0);
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
}
