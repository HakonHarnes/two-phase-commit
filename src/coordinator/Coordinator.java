package twophasecommit.coordinator;

import twophasecommit.Account;
import twophasecommit.LogManager;
import twophasecommit.SubTransaction;
import twophasecommit.Transaction;
import twophasecommit.constants.State;
import twophasecommit.constants.Vote;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The coordinator coordinates the execution of
 * transactions between participants
 *
 * Either all commit, or all abort
 */
class Coordinator {
    private int id;

    //Timeout
    private final int TIMEOUT = 15000;

    /**
     * HashMaps are used here because messages are
     * sent over socket with IDs attached
     *
     * This way, I can do map.get(id) instead of looping
     * through all entries to find the correct object
     */

    /*
     * A map of participants on the form <id, state>
     * Needs to be ConcurrentHashMap because of multithreading
     */
    private ConcurrentHashMap<Integer, State> participants = new ConcurrentHashMap<>();

    //A map of transactions on the form <transaction id, transaction>
    private HashMap<Integer, Transaction> transactions = new HashMap<>();

    //A map of timers on the form <transaction id, timer>
    private HashMap<Integer, Timer> timers = new HashMap<>();

    //A map of accounts on the form <name, account>, used as test data in transactions
    private HashMap<String, Account> accounts = new HashMap<>();

    //MessageHandler for sending messages to participants
    MessageHandler messageHandler;

    Coordinator(int id){
        this.id = id;
    }

    int getId(){
        return id;
    }

    HashMap<String, Account> getAccounts(){
        return accounts;
    }

    void setMessageHandler(MessageHandler messageHandler){
        this.messageHandler = messageHandler;
    }

    void addAccount(Account account){
        accounts.put(account.getName(), account);
    }

    State getParticipantState(int participantId){
        return participants.get(participantId);
    }

    /**
     * Sets state of a participant
     *
     * @param state the state to be set
     * @param id    the id of the participant
     */
    void setParticipantState(State state, int id){
        System.out.println("PARTICIPANT #" + id + ": " + state);

        //Set state, remove if disconnected
        if(state == State.DISCONNECTED) {
            participants.remove(id);
            timers.remove(id);
        }
        else
            participants.put(id, state);
    }

    /**
     * Prepares transaction
     *
     * @param accountA the account a
     * @param accountB the account b
     * @param amount   the amount
     */
    void prepareTransaction(String accountA, String accountB, double amount){

        //Gets test data - if none is provided, create some with balance 100
        Account A = (accounts.get(accountA) != null ? accounts.get(accountA) : new Account(accountA.trim().toUpperCase(), 100));
        Account B = (accounts.get(accountB) != null ? accounts.get(accountB) : new Account(accountB.trim().toUpperCase(), 100));

        //Creates the transaction
        Transaction transaction = new Transaction(A, B, amount);
        System.out.println("\n" + transaction + "\n");
        transactions.put(transaction.getId(), transaction);

        //Creates a timer for the transaction (used for timeout)
        Timer timer = new Timer();
        timers.put(transaction.getId(), timer);

        //Gets the two sub-transactions
        ArrayList<SubTransaction> subTransactions = transaction.getSubTransactions();

        //Gets initialized participants who can execute the sub-transactions
        ArrayList<Integer> readyParticipants = getInitializedParticipants();
        if(readyParticipants.size() < subTransactions.size()) {
            System.out.println("NOT ENOUGH PARTICIPANTS");
            return;
        }

        //Assigns a participant to each sub-transaction
        for(int i = 0; i < subTransactions.size(); i++){
            subTransactions.get(i).setParticipantId(readyParticipants.get(i));
            System.out.println(subTransactions.get(i) + "\n");
        }

        //Writes <T, START> to log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", START") ;

        System.out.println("SENDING SUB-TRANSACTIONS TO PARTICIPANTS...\n");

        //Sends sub-transaction to each assigned participant
        messageHandler.broadcast(transaction);

        //Writes <T, ACCOUNT1, ACCOUNT2, AMOUNT>
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", " + A.getName() + ", " + B.getName() + ", " + transaction.getAmount());

        //Starts timeout
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\nTIMEOUT - PARTICIPANTS TOOK TO LONG");
                abortTransaction(transaction);
            }
        }, TIMEOUT);
    }

    /**
     * Handles vote from a participant
     *
     * @param vote          the vote to handle
     * @param participantId the participant id
     * @param transactionId the transaction id
     */
    void handleVote(Vote vote, int participantId, int transactionId){
        System.out.println("PARTICIPANT #" + participantId + ": VOTED " + vote + " TRANSACTION #" + transactionId);

        //Gets the transaction
        Transaction transaction = transactions.get(transactionId);

        //Aborts transaction
        if(vote == Vote.ABORT)
            abortTransaction(transaction);

        /*
         * Checks if every participant has voted to commit (all are waiting)
         * Commits transaction if true
         */
        else if(checkState(transaction, State.WAITING))
            commitTransaction(transaction);
    }

    /**
     * Checks the state of all participants in a transaction
     *
     * @param transaction the transaction to be checked
     * @param state       the desired state
     * @return            are all participant in the desired state?
     */
    private boolean checkState(Transaction transaction, State state){
        ArrayList<SubTransaction> subTransactions = transaction.getSubTransactions();
        for(SubTransaction subTransaction : subTransactions){
            int participantId = subTransaction.getParticipantId();
            State participantState = participants.get(participantId);

            if(participantState != state) return false;
        }

        return true;
    }

    /**
     * Aborts a transaction
     *
     * @param transaction the transaction to be aborted
     */
    private void abortTransaction(Transaction transaction){

        //Cancels the timeout
        Timer timer = timers.get(transaction.getId());
        timer.cancel();

        //Sends abort-message to each participant
        System.out.println("\nINITIATING GLOBAL ABORT");
        messageHandler.broadcast(Vote.ABORT, transaction);

        //Reads log to know what to undo
        String log = LogManager.readLog(String.valueOf(id));

        //Writes <T, ABORT> to the log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", ABORT");

        //TODO: undo(log)

        //Aborts the transaction
        transaction.abort();

        //Writes <T, END> to the log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", END") ;

        //Forget phase - removes the transaction
        transactions.remove(transaction.getId());
    }

    /**
     * Commits a transaction
     *
     * @param transaction the transaction to be committed
     */
    private void commitTransaction(Transaction transaction){

        //Cancels the timeout
        Timer timer = timers.get(transaction.getId());
        timer.cancel();

        //Sends commit-message to each participant
        System.out.println("\nINITIATING GLOBAL COMMIT\n");
        messageHandler.broadcast(Vote.COMMIT, transaction);

        //Writes <T, COMMIT> to log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", COMMIT") ;

        //Commits the transaction
        transaction.commit();

        //Writes <T, END> to log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", END") ;

        //Forget phase - removes the transaction
        transactions.remove(transaction.getId());
    }

    /**
     * Gets initialized participants
     * These are available for transactions
     *
     * @return ArrayList of participants that are in
     *         the INITIALIZED state
     */
    private ArrayList<Integer> getInitializedParticipants(){
        ArrayList<Integer> initializedParticipants = new ArrayList<>();

        //Checks the state of each participant
        participants.forEach((id, state) -> {
            if(state == State.INITIALIZED) initializedParticipants.add(id);
        });

        return initializedParticipants;
    }
}