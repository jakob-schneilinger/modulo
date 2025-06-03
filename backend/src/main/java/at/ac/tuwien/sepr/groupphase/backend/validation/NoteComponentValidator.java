package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class NoteComponentValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentValidator componentValidator;

    public NoteComponentValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    /**
     * Validates a note component.
     *
     * @param note to be validated
     * @param selfId of the component itself
     */
    public void validateNoteComponent(NoteDto note, long selfId) {
        LOG.trace("validateNoteComponent({})", note);
        List<String> errors = new ArrayList<>(componentValidator.validateComponent(note, selfId));

        if (selfId < 1) {
            if (note.title() == null) {
                errors.add("Note title is null");
            } else if (note.title().isEmpty()) {
                errors.add("Note title is empty");
            }
        }

        if (note.title() != null && note.title().length() > 255) {
            errors.add("Note title exceeds maximum length of 255 characters");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Note component validation failed", errors);
        }
    }
}
