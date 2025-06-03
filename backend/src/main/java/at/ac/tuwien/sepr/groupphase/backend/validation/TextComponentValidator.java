package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class TextComponentValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentValidator componentValidator;

    public TextComponentValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    /**
     * Validates a text component.
     *
     * @param text content
     * @param selfId of the component itself
     */
    public void validateTextComponent(TextDto text, long selfId) {
        LOG.trace("validateTextComponent");
        List<String> errors = new ArrayList<>(componentValidator.validateComponent(text, selfId));

        if (selfId < 1) {
            if (text.content() == null) {
                errors.add("Text content is null");
            } else if (text.content().isEmpty()) {
                errors.add("Text content is empty");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }
}
