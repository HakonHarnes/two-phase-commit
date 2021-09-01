package twophasecommit;

import java.io.Serializable;

/**
 * The sub-transaction is a part of a
 * transaction that is executed by a participant
 */
public class SubTransaction implements Serializable {

    //IDs
    private int participantId;
    private int transactionId;
    private int id;

    //Data
    private Account account;
    private double amount;

    public SubTransaction(int transactionId, int id, Account account, double amount){
        this.transactionId = transactionId;
        this.id = id;

        this.account = account;
        this.amount = amount;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public String getId(){
        return transactionId + "_" + id;
    }

    public int getTransactionId(){
        return transactionId;
    }

    public double getAmount(){
        return amount;
    }

    public Account getAccount(){
        return account;
    }


    //Executes the sub-transaction
    public void execute(){
        if(amount > 0)
            account.deposit(amount);
        else
            account.withdraw(Math.abs(amount));
    }

    //Commits the sub-transaction
    public void commit(){
        System.out.println("\nCOMMITTING SUB-TRANSACTION #" + getId() + "...COMMITTED\n");
    }

    //Aborts the sub-transaction
    public void abort(){
        System.out.println("\nABORTING SUB-TRANSACTION #" + getId() + "...ABORTED\n");
    }

    public String toString(){
        return "-- SUB-TRANSACTION " + getId() + " --\n" + account + (amount > 0 ? " ADD(" : " SUB(") + Math.abs(amount) + "), #" + participantId;
    }
}