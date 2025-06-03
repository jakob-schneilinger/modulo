package at.ac.tuwien.sepr.groupphase.backend.validation;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Component
@Primary
public class ComponentValidator {

    //Maximal depth for rekursive containers (not including root board)
    private static final int MAX_DEPTH = 5;

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final UserService userService;

    @Autowired
    public ComponentValidator(ComponentRepository componentRepository, UserService userService) {
        this.componentRepository = componentRepository;
        this.userService = userService;
    }

    /**
     * Helper method for standard component validation.
     *      Checks for:
     *      - no overlapping
     *      - user is authorized
     *      - parentId does exist and does not equal self id
     *      - column/row/width/height are not below 1
     *      - max depth is not reached
     *      - no circles by containers
     *
     * @param dto to be validated
     * @param selfId od the component (can be -1L for creation)
     */
    @Transactional
    public List<String> validateComponent(ComponentDto dto, long selfId) {
        LOG.trace("validateComponent({}, {})", dto, selfId);
        List<String> errors = new ArrayList<>();

        ComponentDto component = dto;
        Long userId = userService.getUserId();

        if (selfId > 0) {
            Component entity = componentRepository.findById(selfId)
                .orElseThrow(() -> new NotFoundException("Component with ID " + selfId + " not found"));

            if (!entity.getOwnerId().equals(userId)) {
                throw new UserNotAuthorizedException("User is not owner of this component");
            }

            component = new ComponentUpdateDto(selfId,
                dto.parentId() != null ? dto.parentId() : componentRepository.getParentId(selfId),
                dto.width() != null ? dto.width() : entity.getWidth(),
                dto.height() != null ? dto.height() : entity.getHeight(),
                dto.column() != null ? dto.column() : entity.getColumn(),
                dto.row() != null ? dto.row() : entity.getRow()
            );

            if (entity.isContainer()) {
                int upwardDepth = componentRepository.getParentDepth(component.parentId()) + 1;
                int downwardDepth = getMaxChildDepth(entity, 0, component.parentId());

                if (upwardDepth + downwardDepth > MAX_DEPTH) {
                    errors.add("Violates max depth requirement of " + MAX_DEPTH + " components");
                }
            }
        }

        if (component.parentId() != null) {

            if (component.parentId().equals(selfId)) {
                throw new ConflictException("The component cannot be its own parent", List.of());
            }

            Component parent = componentRepository.findById(component.parentId())
                .orElseThrow(() -> new NotFoundException("Parent with given ID does not exist"));

            if (!parent.getOwnerId().equals(userId)) {
                throw new UserNotAuthorizedException("User is not authorized for given parent");
            }

            if (component.row() < 1) {
                errors.add("Row should be greater than zero");
            }

            if (component.column() < 1) {
                errors.add("Column should be greater than zero");
            }

            if (component.width() < 1) {
                errors.add("Width should be greater than zero");
            }

            if (component.height() < 1) {
                errors.add("Height should be greater than zero");
            }

            if (componentRepository.getParentDepth(component.parentId()) + 1 > MAX_DEPTH) {
                errors.add("Violates max depth requirement of " + MAX_DEPTH + " components");
            }

            List<Component> siblings = parent.getChildren().stream()
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