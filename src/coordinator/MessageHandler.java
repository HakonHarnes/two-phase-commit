package twophasecommit.coordinator;

import twophasecommit.SubTransaction;
import twophasecommit.Transaction;
import twophasecommit.constants.State;
import twophasecommit.constants.Vote;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles messages from the participants
 */
class MessageHandler {

    //List of ObjectOutputStreams used for communicating with participants
    private HashMap<Integer, ObjectOutputStream> streams = new HashMap<>();

    //Coordinator to communicate report to
    private Coordinator coordinator;

    public MessageHandler(Coordinator coordinator){
        this.coordinator = coordinator;
    }

    /**
     * Adds connection to new participant
     *
     * @param connection the connection to be added
     */
    void addConnection(Socket connection){
        int participantId = connection.getPort();

        //Adds ObjectOutputStream to list
        try {
            ObjectOutputStream stream = new ObjectOutputStream(connection.getOutputStream());
            streams.put(participantId, stream);
        } catch (IOException e){
            e.printStackTrace();
        }

        //Sends greeting message to participant
        send("CONNECTED TO COORDINATOR #" + coordinator.getId(), participantId);

        //Starts a message listener that continually listens for messages from this participant
        MessageListener listener = new MessageListener(connection, this);
        listener.start();
    }

    /**
     * Remove participant connection
     *
     * @param connection the connection to be removed
     */
    void removeConnection(Socket connection){
        int id = connection.getPort();

        //Remove the stream and set the state to disconnected
        streams.remove(id);
        coordinator.setParticipantState(State.DISCONNECTED, id);

        //Close the connection
        try {
            connection.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Handles state changes from the participants
     *
     * @param state   the state of the participant
     * @param id      the participant id
     */
    void handleState(State state, int id){

        //Set participant state in the coordinator
        coordinator.setParticipantState(state, id);
    }

    /**
     * Handles votes from the participants
     *
     * @param vote          the vote from the participant
     * @param id            the participant id
     * @param transactionId the transaction id
     */
    void handleVote(Vote vote, int id, int transactionId){

        //Handle the vote in the coordinator
        coordinator.handleVote(vote, id, transactionId);
    }

    /**
     * Sends an object (i.e. message, transaction) to a participant
     *
     * @param object the object to be sent
     * @param id     the id of the participant receiver
     */
    void send(Object object, int id){
        try {
            ObjectOutputStream stream = streams.get(id);

            if(stream != null)
                stream.writeObject(object);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts an object (i.e. message) to all participants in a transaction
     *
     * @param object      the object to be sent
     * @param transaction the transaction the participants belong to
     */
    void broadcast(Object object, Transaction transaction){
        ArrayList<SubTransaction> subTransactions = transaction.getSubTransactions();

        for(SubTransaction subTransaction : subTransactions)
            send(object, subTransaction.getParticipantId());

    }

    /**
     * Broadcasts sub-transactions all participants in a transaction
     *
     * @param transaction the transaction
     */
    void broadcast(Transaction transaction){
        ArrayList<SubTransaction> subTransactions = transaction.getSubTransactions();

        for(SubTransaction subTransaction : subTransactions){
            send(subTransaction, subTransaction.getParticipantId());
        }
    }
}