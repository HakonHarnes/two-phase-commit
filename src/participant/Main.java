package twophasecommit.participant;

import twophasecommit.constants.State;

import java.net.Socket;

/**
 * Does initial setup:
 * - Connects to server
 * - Creates a participant
 * - Creates CommandHandler and MessageHandler for the participant
 */
class Main {
    /**
     * Main.
     *
     * @param args the args
     */
    public static void main(String[] args){
        final String IP_ADDRESS = "localhost";
        final int PORT = 1250;

        try {
            //Initializes connection
            Socket connection = new Socket(IP_ADDRESS, PORT);

            //Creates a participant
            int id = connection.getLocalPort();
            Participant participant = new Participant(id);

            //Listens and handles commands from the user, reports to participant
            CommandHandler commandHandler = new CommandHandler(participant);
            commandHandler.init();

            ///Listens and handles messages from the coordinator, reports to participant
            MessageHandler messageHandler = new MessageHandler(participant, connection);
            participant.setMessageHandler(messageHandler);
            participant.setState(State.INITIALIZED);
        } catch(Exception e){
            System.out.println("CONNECTION UNSUCCESSFUL");
            e.printStackTrace();
        }
    }
}