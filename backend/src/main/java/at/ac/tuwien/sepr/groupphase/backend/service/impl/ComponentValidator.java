package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ContainerDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ComponentValidator {

    //Maximal depth for rekursive containers (not including root board)
    private static final int MAX_DEPTH = 5;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;

    public ComponentValidator(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
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
        errors.addAll(validateContainerForCreation(board));
        errors.addAll(validateComponent(board, userId, null));

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

        errors.addAll(validateContainerForUpdate(boardDto, board));
        errors.addAll(validateComponent(boardDto, userId, board.getId()));

        if (boardDto.name() == null || boardDto.name().isEmpty()) {
            errors.add("Board name is null or empty");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation for updating board failed", errors);
        }
    }

    private List<String> validateComponent(ComponentDto component, long userId, Long selfId) {
        LOG.trace("validateComponent({}, {})", component, selfId);
        List<String> errors = new ArrayList<>();
        if (component.parentId() != null) {
            Optional<Component> parentComponentOpt = componentRepository.findById(component.parentId());

            if (parentComponentOpt.isEmpty()) {
                throw new NotFoundException("Parent with given ID does not exist");
            }

            Component parentComponent = parentComponentOpt.get();

            if (!parentComponent.getOwnerId().equals(userId)) {
                throw new UserNotAuthorizedException("User is not authorized for given parent");
            }

            List<Component> siblings = parentComponent.getChildren().stream().filter(child -> !Objects.equals(child.getId(), selfId)).toList();

            long x1 = component.column();
            long y1 = component.row();
            long w1 = component.width();
            long h1 = component.height();

            for (Component sibling : siblings) {
                long x2 = sibling.getColumn();
                long y2 = sibling.getRow();
                long w2 = sibling.getWidth();
                long h2 = sibling.getHeight();

                boolean overlaps = !(x1 + w1 <= x2 || x1 >= x2 + w2 || y1 + h1 <= y2 || y1 >= y2 + h2);

                if (overlaps) {
                    errors.add("Component overlaps with existing component (ID: " + sibling.getId() + ")");
                }
            }
        }
        return errors;
    }

    private List<String> validateContainerForCreation(ContainerDto component) {
        LOG.trace("validateContainerForCreation({})", component);
        List<String> errors = new ArrayList<>();
        if (componentRepository.getParentDepth(component.parentId()) + 1 > MAX_DEPTH) {
            errors.add("Violates max depth requirement of " + MAX_DEPTH + " components");
        }
        return errors;
    }

    private List<String> validateContainerForUpdate(ContainerDto component, Component self) {
        LOG.trace("validateContainerForUpdate({}, {})", component, self);

        List<String> errors = new ArrayList<>();
        if (Objects.equals(component.parentId(), self.getId())) {
            throw new ConflictException("Circular structure found", new ArrayList<>());
        }

        int upwardDepth = componentRepository.getParentDepth(component.parentId()) + 1;
        int downwardDepth = getMaxChildDepth(self, 0, component.parentId());

        if (upwardDepth + downwardDepth > MAX_DEPTH) {
            errors.add("Violates max depth requirement of " + MAX_DEPTH + " components");
        }
        return errors;
    }

    private int getMaxChildDepth(Component component, int currentDepth, Long parentId) {
        LOG.trace("getMaxChildDepth({}, {}, {})", component, currentDepth, parentId);

        if (Objects.equals(component.getId(), parentId)) {
            throw new ConflictException("Circular structure found", new ArrayList<>());
        }

        if (!component.isContainer()) {
            return 0;
        }

        int maxDepth = 0;
        for (Component child : component.getChildren()) {
            int depth = 1 + getMaxChildDepth(child, currentDepth + 1, parentId);
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth;
    }
}
