package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Component
public class BoardComponentValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentValidator componentValidator;

    public BoardComponentValidator(ComponentValidator componentValidator) {
        this.componentValidator = componentValidator;
    }

    /**
     * Validates a board component.
     *
     * @param board to be validated
     * @param selfId of the component itself
     */
    public void validateBoardComponent(BoardDto board, long selfId) {
        LOG.trace("validateBoard({})", board);

        List<String> errors = new ArrayList<>(componentValidator.validateComponent(board, selfId));

        if (selfId < 1) {
            if (board.name() == null) {
                errors.add("Board name is null");
            } else if (board.name().isEmpty()) {
                errors.add("Board name is empty");
            }
        }

        if (board.name() != null && board.name().length() > 255) {
            errors.add("Board name exceeds maximum length of 255 characters");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }
}
