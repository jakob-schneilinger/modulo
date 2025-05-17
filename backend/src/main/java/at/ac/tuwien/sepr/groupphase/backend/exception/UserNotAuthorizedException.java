package at.ac.tuwien.sepr.groupphase.backend.exception;

/**
 * Exception that signals that the user does not have the necessary
 * permissions to access or modify the requested resource.
 */
public class UserNotAuthorizedException extends RuntimeException {

    /**
     * Constructs a new UserNotAuthorizedException with the specified detail message.
     *
     * @param message The detail message that explains the reason for the exception.
     */
    public UserNotAuthorizedException(String message) {
        super(message);
    }
}
