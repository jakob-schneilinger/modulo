package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
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
     * Validates a board that should be created.
     *
     * @param board to be created
     * @param userId of the user that wants to create the board
     */
    public void validateBoardForCreation(BoardCreateDto board, long userId) {
        LOG.trace("validateBoardForCreation({})", board);

        List<String> errors = new ArrayList<>();
        errors.addAll(componentValidator.validateContainerForCreation(board));
        errors.addAll(componentValidator.validateComponent(board, userId, null));

        if (board.name() == null || board.name().isEmpty()) {
            errors.add("Board name is null or empty");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }

    /**
     * Validates a board that should be updated.
     *
     * @param boardDto dto for board that should be created
     * @param board current board entity in database
     * @param userId of the user that wants to create the board
     */
    public void validateBoardForUpdate(BoardUpdateDto boardDto, Component board, long userId) {
        LOG.trace("validateBoardForUpdate({}, {})", boardDto, board);

        List<String> errors = new ArrayList<>();
        if (!board.getOwnerId().equals(userId)) {
            throw new UserNotAuthorizedException("User is not owner of this component");
        }

        errors.addAll(componentValidator.validateContainerForUpdate(boardDto, board));
        errors.addAll(componentValidator.validateComponent(boardDto, userId, board.getId()));

        if (boardDto.name() == null || boardDto.name().isEmpty()) {
            errors.add("Board name is null or empty");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }
}
