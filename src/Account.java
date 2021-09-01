package twophasecommit;

import java.io.Serializable;

/**
 * A simple class used for test data
 */
public class Account implements Serializable {
    private String name;
    private double balance;

    public Account(String name, double balance){
        this.name = name;
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Deposit money to account
     *
     * @param amount the amount to deposit
     */
    public void deposit(double amount){
        balance += amount;
    }

    /**
     * Withdraw money from account
     *
     * @param amount the amount to withdraw
     */
    public void withdraw(double amount){
       balance -= amount;
    }

    public String toString(){
        return name + ", " + balance;
    }
}