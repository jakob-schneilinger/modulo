package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * This exception is used for errors occurring when an user doesn't have permissions for the executed action.
 */
public class ForbiddenException extends RuntimeException {
    /**
     * Constructs a new ForbiddenException with the a message why the action went wrong.
     *
     * @param message A message describing the exception.
     */
    public ForbiddenException(String message) {
        super("Forbidden: " + message);
    }
}
