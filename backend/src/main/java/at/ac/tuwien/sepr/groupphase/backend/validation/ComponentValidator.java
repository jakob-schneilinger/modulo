package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ContainerDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     * Helper method for standard component validation.
     *      Checks for:
     *      - no overlapping
     *      - user is authorized
     *      - parentId does exist and does not equal self id
     *      - column and row are not below 1
     *
     * @param component to be validated
     * @param userId of the user
     * @param selfId od the component (can be -1L for creation)
     * @return List of errors found
     */
    @Transactional
    public List<String> validateComponent(ComponentDto component, long userId, Long selfId) {
        LOG.trace("validateComponent({}, {})", component, selfId);
        List<String> errors = new ArrayList<>();
        if (component.parentId() != null) {
            Optional<Component> parentComponentOpt = componentRepository.findById(component.parentId());

            if (parentComponentOpt.isEmpty()) {
                throw new NotFoundException("Parent with given ID does not exist");
            }

            if (Objects.equals(component.parentId(), selfId)) {
                throw new ConflictException("The component cannot be its own parent", new ArrayList<>());
            }

            Component parentComponent = parentComponentOpt.get();

            if (!parentComponent.getOwnerId().equals(userId)) {
                throw new UserNotAuthorizedException("User is not authorized for given parent");
            }

            if (component.row() < 1) {
                errors.add("Row should be greater than zero");
            }

            if (component.column() < 1) {
                errors.add("Column should be greater than zero");
            }

            List<Component> siblings = parentComponent.getChildren().stream()
                    .filter(child -> !Objects.equals(child.getId(), selfId)).toList();

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
                    throw new ConflictException("Conflict while trying to update a component", List.of("Component overlaps with existing component"));
                }
            }
        }
        return errors;
    }

    /**
     * Helper method for standard Container validation for creation.
     *      Checks for:
     *      - maximal depth
     *
     * @param component to be validated
     * @return List of errors found
     */
    public List<String> validateContainerForCreation(ContainerDto component) {
        LOG.trace("validateContainerForCreation({})", component);
        List<String> errors = new ArrayList<>();
        if (componentRepository.getParentDepth(component.parentId()) + 1 > MAX_DEPTH) {
            errors.add("Violates max depth requirement of " + MAX_DEPTH + " components");
        }
        return errors;
    }

    /**
     * Helper method for standard Container validation for update.
     *      Checks for:
     *      - maximal depth
     *      - no circular structures
     *
     * @param component to be validated
     * @param self entity of the component
     * @return List of errors found
     */
    @Transactional
    public List<String> validateContainerForUpdate(ComponentDto component, Component self) {
        LOG.trace("validateContainerForUpdate({}, {})", component, self);

        List<String> errors = new ArrayList<>();

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
