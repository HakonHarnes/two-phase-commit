package twophasecommit.coordinator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Does initial setup:
 * - Starts a server that participants can connect to
 * - Creates a coordinator for the participants
 * - Creates CommandHandler and MessageHandler for the coordinator
 */
class Main {
    public static void main(String[] args) {
        final int PORT = 1250;

        try {
            System.out.println("WAITING FOR PARTICIPANTS...\n");

            //Starts a server that the participants can connect to
            ServerSocket serverSocket = new ServerSocket(PORT);

            //Creates a coordinator
            Coordinator coordinator = new Coordinator(PORT);

            //Listens and handles commands from the user, reports to coordinator
            CommandHandler commandHandler = new CommandHandler(coordinator);
            commandHandler.init();

            //Listens and handles messages from participants, reports to the coordinator
            MessageHandler messageHandler = new MessageHandler(coordinator);
            coordinator.setMessageHandler(messageHandler);

            //Accepts participants
            while (true) {
                Socket connection = serverSocket.accept();
                messageHandler.addConnection(connection);
            }
        } catch (IOException e) {
            System.out.println("COULD NOT START SERVER");
            e.printStackTrace();
        }
    }
}