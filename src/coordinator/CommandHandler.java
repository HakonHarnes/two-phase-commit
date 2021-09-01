package twophasecommit.coordinator;

import twophasecommit.Account;
import twophasecommit.constants.Command;
import twophasecommit.constants.State;

import java.util.HashMap;

/**
 * Handles commands from the user
 */
class CommandHandler {

    //Coordinator we are handling commands for
    private Coordinator coordinator;

    CommandHandler(Coordinator coordinator){
        this.coordinator = coordinator;
    }

    //Starts a command listener that continually listens for commands
    void init(){
        CommandListener listener = new CommandListener(this);
        listener.start();
    }

    /**
     * Handles the command appropriately
     *
     * @param command the command to execute
     * @param args    the args for the command
     */
    void handleCommand(Command command, String[] args){
        switch(command){

            //Displays help
            case HELP:
                System.out.println(
                          "\n         COMMAND                            DESC"
                        + "\n- ACCOUNT <NAME> <BALANCE>              CREATE ACCOUNT"
                        + "\n- TRANSACTION <ACC1> <ACC2> <AMOUNT>    START TRANSACTION"
                        + "\n- STATE <PARTICIPANT_ID>                DISPLAY PARTICPANT STATE"
                        + "\n- DATA                                  DISPLAY TEST DATA"
                        + "\n- ID                                    DISPLAY COORDINATOR ID"
                        + "\n- HELP                                  DISPLAY HELP\n");
                break;

            //Creates an account
            case ACCOUNT:

                //Checks that arguments are passed in correctly
                if(args.length > 2){
                    System.out.println("TOO MANY ARGUMENTS\n");
                    return;
                }
                else if(args.length < 2) {
                    System.out.println("TOO FEW ARGUMENTS\n");
                    return;
                }

                //Creates a new account if the balance argument is OK
                double balance = convertToDouble(args[1]);
                if(balance < 0)
                    System.out.println("INVALID BALANCE");
                else {
                    String name = args[0].trim().toUpperCase();
                    coordinator.addAccount(new Account(name, balance));
                    System.out.println("ACCOUNT '" + name + "' ADDED\n");
                }

                break;

            //Starts a transaction
            case TRANSACTION:

                //Checks that arguments are passed in correctly
                if(args.length > 3){
                    System.out.println("TOO MANY ARGUMENTS\n");
                    return;
                }
                else if(args.length < 3){
                    System.out.println("TOO FEW ARGUMENTS\n");
                    return;
                }

                //Creates a new transaction if the amount argument is OK
                double amount = convertToDouble(args[2]);
                if(amount < 0)
                    System.out.println("INVALID AMOUNT\n");
                else
                    coordinator.prepareTransaction(args[0], args[1], amount);

                break;

            //Displays the state of a participant
            case STATE:

                //Checks that arguments are passed in correctly
                if(args.length > 1){
                    System.out.println("TOO MANY ARGUMENTS\n");
                    return;
                }
                else if(args.length < 1){
                    System.out.println("TOO FEW ARGUMENTS\n");
                    return;
                }

                //Gets state for participant
                int id = convertToInt(args[0]);
                State state = coordinator.getParticipantState(id);

                //Prints the state
                if(state == null)
                    System.out.println("INVALID ID\n");
                else
                    System.out.println("PARTICIPANT #" + id + ": " + state + "\n");
                break;

            //Displays account data
            case DATA:

                //Gets accounts
                HashMap<String, Account> accounts = coordinator.getAccounts();
                if(accounts.size() == 0) System.out.println("NO TEST DATA\n");

                //Prints the account data
                for(Account account : accounts.values())
                    System.out.println(account);

                System.out.println();
                break;

            //Displays coordinator ID
            case ID:
                System.out.println("ID: #" + coordinator.getId() + "\n");
                break;
        }
    }

    /**
     * Converts String to double
     *
     * @param input the string to be converted
     * @return      the input as a double
     */
    private double convertToDouble(String input){
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e){
            return -1;
        }
    }

    /**
     * Converts String to int
     *
     * @param input the string to be converted
     * @return      the input as an int
     */
    private int convertToInt(String input){
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e){
            return -1;
        }
    }
}