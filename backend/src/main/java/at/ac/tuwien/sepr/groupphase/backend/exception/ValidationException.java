package at.ac.tuwien.sepr.groupphase.backend.exception;

public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Exception e) {
        super(e);
    }
}
