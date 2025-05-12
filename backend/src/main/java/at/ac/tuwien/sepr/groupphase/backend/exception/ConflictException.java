package at.ac.tuwien.sepr.groupphase.backend.exception;

import java.util.List;

/**
 * Exception that signals, that data,
 * that came from outside the backend, conflicts with the current state of the system.
 * The data violates some constraint on relationships
 * (rather than an invariant).
 * Contains a list of all conflict checks that failed when validating the piece of data in question.
 */
public class ConflictException extends ErrorListException {

    /**
     * Constructs a new ConflictException with the specified summary message and list of errors.
     *
     * @param messageSummary A summary message describing the conflict.
     * @param errors A list of specific error messages indicating the failed conflict checks.
     */
    public ConflictException(String messageSummary, List<String> errors) {
        super("Conflicts", messageSummary, errors);
    }
}
