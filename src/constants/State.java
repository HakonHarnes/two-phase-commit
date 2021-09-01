package twophasecommit.constants;

/**
 * This enum contains a list of states
 * that a participant can be in
 */
public enum State {
    INITIALIZED,
    PREPARING,
    VOTING,
    WAITING,
    ABORT,
    COMMIT,
    DISCONNECTED
    }