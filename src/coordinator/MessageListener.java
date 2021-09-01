package twophasecommit.coordinator;

import twophasecommit.constants.Vote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Continually listens for messages from a participant
 */
class MessageListener extends Thread {

    //Connection to participant
    private Socket connection;

    //The MessageHandler to report to
    private MessageHandler handler;

    public MessageListener(Socket connection, MessageHandler handler){
        this.connection = connection;
        this.handler = handler;
    }

    //Listens for messages
    public void run(){
        try (
                //Initializes the reader
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader)
        ){
            String line = reader.readLine();
            while(line != null) {
                String[] message = line.split(":");

                //Message on the form 'vote:participantId:transactionId'
                if(message.length > 2) {
                    Vote vote = convertToVote(message[0]);

                    //If vote is valid, pass it on to the MessageHandler
                    if(vote != null)
                        handler.handleVote(vote, Integer.parseInt(message[1]), Integer.parseInt(message[2]));
                }

                //Message on the form 'state:participantId'
                else if(message.length > 1) {
                    twophasecommit.constants.State state = convertToState(message[0]);

                    //If state is valid, pass it on to the MessageHandler
                    if(state != null)
                        handler.handleState(state, Integer.parseInt(message[1]));
                }

                line = reader.readLine();
            }

        }
        catch(IOException e){
            e.printStackTrace();
        }

        //Participant has disconnected - remove the connection
        handler.removeConnection(connection);
    }

    /**
     * Converts String to State
     *
     * @param input the string to be converted
     * @return      the input as a State
     */
    private twophasecommit.constants.State convertToState(String input){
        try {
            return twophasecommit.constants.State.valueOf(input);
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    /**
     * Converts String to Vote
     *
     * @param input the input to be converted
     * @return      the input as a Vote
     */
    private Vote convertToVote(String input){
        try {
            return Vote.valueOf(input);
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}