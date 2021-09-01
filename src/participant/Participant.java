package twophasecommit.participant;

import twophasecommit.LogManager;
import twophasecommit.SubTransaction;
import twophasecommit.constants.State;
import twophasecommit.constants.Vote;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The participant executes a sub-transaction
 * provided by the coordinator
 *
 * Execution is controlled by the coordinator,
 * either commit or abort
 */
class Participant {
    private int id;

    //Timeout >= Coordinator timeout
    private final int TIMEOUT = 15000;
    Timer timer = new Timer();

    //MessageHandler for sending messages to coordinator
    private MessageHandler messageHandler;

    //The sub-transaction to execute
    private SubTransaction transaction;

    //State of the participant
    private State state = State.INITIALIZED;

    Participant(int id){
        this.id = id;
    }

    int getId(){
        return id;
    }

    void setMessageHandler(MessageHandler messageHandler){
        this.messageHandler = messageHandler;
    }

    State getState(){
        return state;
    }

    /**
     * Sets the state
     *
     * @param state state to be set
     */
    void setState(State state){
        System.out.println("STATE: " + state);

        this.state = state;

        //Reports to the coordinator that there is a new state
        messageHandler.sendState(state);
    }

    /**
     * Prepares the sub-transaction
     *
     * @param transaction the sub-transaction to be prepared
     */
    void prepareTransaction(SubTransaction transaction){
        //State is now preparing
        setState(State.PREPARING);

        //Prints sub-transaction
        this.transaction = transaction;
        System.out.println("\n" + transaction + "\n");

        //Writes <T, START> to the log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", START");

        //Gets the old transaction value
        double oldValue = transaction.getAccount().getBalance();

        //Executes transaction
        transaction.execute();
        System.out.println("EXECUTED SUB-TRANSACTION #" + transaction.getId() + "\n");

        //Writes <T, ACCOUNT, OLD VALUE, NEW VALUE> to the log
        double newValue = transaction.getAccount().getBalance();
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", " + transaction.getAccount().getName() + ", " + oldValue + ", " + newValue);

        setState(State.VOTING);
        System.out.println("\nTYPE 'Y/YES' FOR COMMIT OR 'N/NO' FOR ABORT");
    }

    /**
     * Handles vote from the user
     *
     * @param vote the vote to be handled
     */
    void handleVote(Vote vote){

        //If the state is not voting, the users vote will be turned down
        if(state != State.VOTING){
            System.out.println("\nCAN'T VOTE NOW\n");
            return;
        }

        System.out.println();

        //Waits for decision from coordinator
        setState(State.WAITING);

        //Sends vote to coordinator
        System.out.println("VOTED: " + vote);
        messageHandler.sendVote(vote, transaction.getTransactionId());

        //Starts timeout
        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("\nTIMEOUT - COORDINATOR TOOK TOO LONG");
                abortTransaction();
            }
        }, TIMEOUT);
    }

    /**
     * Handles decision from the coordinator
     *
     * @param decision the decision, either commit or abort the sub-transaction
     */
    void handleDecision(Vote decision){
        System.out.println("DECISION: " + decision + "\n");

        //Cancels timeout
        timer.cancel();

        //Commits or aborts transaction
        if(decision == Vote.COMMIT) commitTransaction();
        else abortTransaction();
    }

    //Commits the sub-transaction
    private void commitTransaction(){

        //Writes to <T, COMMIT> the log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", COMMIT");

        //Commits the sub-transaction
        setState(State.COMMIT);
        transaction.commit();

        //Writes <T, END> to the log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", END");

        //Forget phase - removes the sub-transaction
        this.transaction = null;
        setState(State.INITIALIZED);
    }


    //Aborts the transaction
    void abortTransaction() {

        //Reads log to know what to undo
        String log = LogManager.readLog(String.valueOf(id));

        //Writes <T, ABORT> to log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", ABORT" );

        //TODO: undo(log)

        //Aborts the transaction
        setState(State.ABORT);
        transaction.abort();

        //Writes <T, END> to the log
        LogManager.writeLog(String.valueOf(id), transaction.getId() + ", END" );

        //Forget phase - removes the sub-transaction
        this.transaction = null;
        setState(State.INITIALIZED);
    }
}