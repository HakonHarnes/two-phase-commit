package twophasecommit.participant;

import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Continually listens for messages from the coordinator
 */
class MessageListener extends Thread {

    //ObjectInputStream used for listening to messages
    ObjectInputStream stream;

    //The MessageHandler to report to
    private MessageHandler handler;


    public MessageListener(Socket connection, MessageHandler handler){
        this.handler = handler;

        try {
            this.stream = new ObjectInputStream(connection.getInputStream());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //Listens for messages
    public void run(){
        try {
            Object object = stream.readObject();
            while(object != null){
                handler.handleMessage(object);
                object = stream.readObject();
            }
        } catch (Exception e){
            handler.closeConnection();
        }
    }
}