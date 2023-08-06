package main;

import exceptions.InsufficientBalanceException;
import exceptions.NegativeAmountException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class BankAccount {
    private static final Logger logger = LoggerFactory.getLogger(BankAccount.class);
    private static final AtomicInteger idGenerator = new AtomicInteger();

    private double balance;
    private final int id;

    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
        this.id = idGenerator.getAndIncrement();
        logger.info("A new bank account was created with id {}. Initial balance: {}", id, initialBalance);
    }

    public void deposit(double amount) {
        synchronized (this) {
            handleNegativeAmount(amount);
            this.balance += amount;
            logger.info("Account {}. Money deposited. Amount: {}. New balance: {}", this.getId(), amount, this.balance);
        }
    }

    public void withdraw(double amount) {
        synchronized (this) {
        	handleNegativeAmount(amount);
            handleInsufficientBalance(amount);
            this.balance -= amount;
            logger.info("Account {}. Money withdrawn. Amount: {}. New balance: {}", this.getId(), amount, this.balance);
        }
    }

    public void transfer(BankAccount destination, double amount) {
    	// lock the account with the lower id first to prevent the possibility of a deadlock
    	// two-step locking
        BankAccount first = this.id < destination.id ? this : destination;
        BankAccount second = this.id > destination.id ? this : destination;

        synchronized (first) {
            synchronized (second) {
                handleNegativeAmount(amount);
                handleInsufficientBalance(amount);
                this.withdraw(amount);
                destination.deposit(amount);
                logger.info("Account {}. Money transferred. Amount: {}. New balance: {}. Destination account: {}", this.getId(), amount, this.balance, destination.getId());
            }
        }
    }
    
    public void handleNegativeAmount(double amount) {
    	synchronized (this) {
	    	if (amount < 0) {
	    		logger.error("Account {}. Attempted to deposit negative amount: {}", this.getId(), amount);
	    		throw new NegativeAmountException("Account " + this.getId() + ". Cannot deposit negative amount.");
	    	}
    	}
    }
    
    public void handleInsufficientBalance(double amount) {
    	synchronized (this) {
	    	if (amount > this.balance) {
	    		logger.error("Account {}. Attempted to withdraw more than current balance. Withdrawal amount: {}. Current balance: {}", this.getId(), amount, this.balance);
	    		throw new InsufficientBalanceException("Account " + this.getId() + ". Cannot withdraw more than current balance.");
	    	}
    	}
    }
    
    public double getBalance() {
        synchronized (this) {
            logger.info("Account {}. Balance checked. Current balance: {}", this.getId(), this.balance);
            return this.balance;
        }
    }
    
    public int getId() {
    	synchronized (this) {
    		return this.id;
    	}
    }
}
