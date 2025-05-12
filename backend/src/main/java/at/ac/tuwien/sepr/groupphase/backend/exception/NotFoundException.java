package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * Exception that signals, that whatever resource,
 * that has been tried to access,
 * was not found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a new NotFoundException with the specified detail message.
     *
     * @param message The detail message that explains the reason for the exception
     */
    public NotFoundException(String message) {
        super(message);
    }
}
