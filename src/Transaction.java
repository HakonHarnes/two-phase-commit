package twophasecommit;

import java.util.ArrayList;

/**
 * A transaction that contains two
 * sub-transactions for participants to execute
 */
public class Transaction {

    //IDs
    private static int transactionCount = 0;
    private int subtransactionCount = 0;
    private int id;

    //Accounts
    private Account A;
    private Account B;

    //Amount to transfer
    private double amount;

    //Sub-transactions
    private ArrayList<SubTransaction> subTransactions = new ArrayList<>();

    public Transaction(Account A, Account B, double amount){
        this.id = ++transactionCount;

        this.A = A;
        this.B = B;

        this.amount = amount;

        //Creates two sub-transactions
        subTransactions.add(new SubTransaction(id, ++subtransactionCount, A,   Math.abs(amount)));
        subTransactions.add(new SubTransaction(id, ++subtransactionCount, B, - Math.abs(amount)));
    }

    public int getId(){
        return id;
    }

    public double getAmount(){
        return amount;
    }

    public ArrayList<SubTransaction> getSubTransactions(){
        return subTransactions;
    }

    //Commits the transaction
    public void commit(){
        System.out.println("COMMITTING TRANSACTION #" + getId() + "...COMMITTED\n");
    }

    //Aborts the transaction
    public void abort(){
        System.out.println("ABORTING TRANSACTION #" + getId() + "...ABORTED\n");
    }

    public String toString(){
        return "-- TRANSACTION " + id + " --\n" + A.getName() + " -> " + B.getName() + ", " + amount;
    }
}