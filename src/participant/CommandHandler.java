package twophasecommit.participant;

import twophasecommit.constants.Command;
import twophasecommit.constants.Vote;

/**
 * Handles commands from the user
 */
class CommandHandler {

    //Participant we are handling commands for
    private Participant participant;

    CommandHandler(Participant participant) {
        this.participant = participant;
    }

   //Starts a command listener that continually listens for commands
    void init() {
        CommandListener listener = new CommandListener(this);
        listener.start();
    }

    /**
     * Handles the command appropriately
     *
     * @param command the command to execute
     */
    void handleCommand(Command command) {
        switch (command) {

            //Displays help
            case HELP:
                System.out.println(
                                  "\n COMMAND           DESC"
                                + "\n- STATE      DISPLAY CURRENT STATE"
                                + "\n- YES        VOTE YES"
                                + "\n- NO         VOTE NO"
                                + "\n- ID         DISPLAY PARTICIPANT ID"
                                + "\n- HELP       DISPLAY HELP\n");
                break;

            //Displays participant state
            case STATE:
                System.out.println("STATE: " + participant.getState() + "\n");
                break;

            //Displays participant ID
            case ID:
                System.out.println("ID: #" + participant.getId() + "\n");
                break;
        }
    }

    /**
     * Handles vote from user, passes it on
     * to participant
     *
     * @param vote the vote
     */
    void handleVote(Vote vote) {
        participant.handleVote(vote);
    }
}