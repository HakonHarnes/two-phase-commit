package twophasecommit.participant;


import twophasecommit.SubTransaction;
import twophasecommit.constants.State;
import twophasecommit.constants.Vote;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Handles messages from the coordinator
 */
class MessageHandler {

    //Participant we are handling messages for
    private Participant participant;

    //Connection to coordinator
    Socket connection;

    public MessageHandler(Participant participant, Socket connection){
        this.participant = participant;
        this.connection = connection;

        //Starts listener that continually listens for messages from the coordinator
        MessageListener listener = new MessageListener(connection, this);
        listener.start();
    }

    //Closes connection to coordinator
    void closeConnection(){

        //Gets the state
        State state = participant.getState();

        //Sets state as disconnected
        participant.setState(State.DISCONNECTED);

        //Close the connection
        try {
            connection.close();
        } catch (IOException e){
            e.printStackTrace();
        }

        //Aborts the transaction
        if(state != State.INITIALIZED)
            participant.abortTransaction();

        System.exit(-1);
    }

    /**
     * Handles messages from coordinator
     *
     * @param object the object that has been sent
     */
    void handleMessage(Object object){

        //Message is a sub-transaction - tells participant to prepare it
        if(object instanceof SubTransaction)
            participant.prepareTransaction((SubTransaction) object);

        //Message is a decision to commit or abort - passes it on to participant
        else if(object instanceof Vote)
            participant.handleDecision((Vote) object);

        //Message is a string
        else if(object instanceof String){
            String message = (String) object;
            System.out.println(message);
        }

    }

    /**
     * Sends state to coordinator on the format:
     * 'state:participantId'
     *
     * @param state the state to be sent
     */
    void sendState(State state){
        try {
            PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);
            writer.println(state + ":" + participant.getId());
        } catch (IOException e){
            System.out.println("COULD NOT SEND STATE: " + state);
        }
    }

    /**
     * Sends vote to the coordinator on the format:
     * 'state:participantId:vote'
     *
     * @param vote          the vote to be sent
     * @param transactionId the transaction we are voting for
     */
    void sendVote(Vote vote, int transactionId){
        try {
            PrintWriter writer = new PrintWriter(connection.getOutputStream(), true);
            writer.println(vote + ":" + participant.getId() + ":" + transactionId);
        } catch (IOException e){
            System.out.println("COULD NOT SEND VOTE: " + vote);
        }
    }
}