package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * This exception is used for errors occurring when an user isn't authorized.
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * Constructs a new UnauthorizedException with the a message why the action went wrong.
     *
     * @param message A message describing the exception.
     */
    public UnauthorizedException(String message) {
        super("Unauthorized: " + message);
    }
}
