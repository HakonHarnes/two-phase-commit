package twophasecommit.participant;

import twophasecommit.constants.Command;
import twophasecommit.constants.Vote;

import java.util.Scanner;

/**
 * Continually listens for commands (through terminal) from the user
 */
class CommandListener extends Thread{

    //The CommandHandler we are reporting to
    private CommandHandler handler;

    public CommandListener(CommandHandler handler){
        this.handler = handler;
    }

    //Listens for commands
    public void run(){
        Scanner scanner = new Scanner(System.in);

        String line = scanner.nextLine();
        while(line != null) {

            //Converts message into Command or Vote
            Command command = convertToCommand(line);
            Vote vote = convertToVote(line);

            //Passes on to CommandHandler as Command or Vote
            if(command == Command.HELP || command == Command.STATE || command == Command.ID)
                handler.handleCommand(command);
            else if(vote != null)
                handler.handleVote(vote);
            else
                System.out.println("INVALID COMMAND - TYPE 'HELP'\n");

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
            return Command.valueOf(input.toUpperCase().trim());
        } catch (IllegalArgumentException e){
            return null;
        }
    }

    /**
     * Converts String to Vote
     *
     * @param input the string to be converted
     * @return      the input as a Vote
     */
    private Vote convertToVote(String input){
        if(input.equalsIgnoreCase("YES") || input.equalsIgnoreCase("Y"))
            return Vote.COMMIT;

        if(input.equalsIgnoreCase("NO") || input.equalsIgnoreCase("N"))
            return Vote.ABORT;

        return null;
    }
}