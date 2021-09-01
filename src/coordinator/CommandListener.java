package twophasecommit.coordinator;

import twophasecommit.constants.Command;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Continually listens for commands (through terminal) from the user
 */
class CommandListener extends Thread {

    //The CommandHandler we are reporting to
    private CommandHandler handler;

     CommandListener(CommandHandler handler){
        this.handler = handler;
    }

    //Listens for commands
    public void run(){
        Scanner scanner = new Scanner(System.in);

        String line = scanner.nextLine();
        while(line != null) {
            String[] split = line.split(" ");

            //Converts message into command and arguments
            Command command = convertToCommand(split[0]);
            String[] args = split.length > 1 ? Arrays.copyOfRange(split, 1, split.length) : new String[]{};

            //If command is valid, handle it
            if(command != null) handler.handleCommand(command, args);
            else System.out.println("INVALID COMMAND - TYPE 'HELP'\n");

            line = scanner.nextLine();
        }
    }

    /**
     * Converts String to Command
     *
     * @param input the string to be converted
     * @return      the input as a Command
     */
    private Command convertToCommand(String input){
        try {
            return Command.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}