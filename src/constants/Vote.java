package twophasecommit.constants;

/**
 * This enum contains a list of votes
 * for a transaction, either commit
 * the transaction, or abort it
 */
public enum Vote {
    ABORT,
    COMMIT
}