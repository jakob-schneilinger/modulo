package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ws.ComponentUpdateWsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ws.ComponentUpdateWsType;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Image;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.MyCalendar;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Permission;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.validation.ComponentValidator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ComponentServiceImpl implements ComponentService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final UserService userService;
    private final ComponentValidator componentValidator;
    private final ComponentUpdateNotifier updateNotifier;
    private final PermissionRepository permissionRepository;

    @Value("${global.location.image}")
    private String imagePath;
    @Value("${global.location.calendar}")
    private String calendarPath;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, UserService userService,
            ComponentValidator validator, ComponentUpdateNotifier updateNotifier, PermissionRepository permissionRepository) {
        this.componentRepository = componentRepository;
        this.userService = userService;
        this.componentValidator = validator;
        this.updateNotifier = updateNotifier;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public ComponentDetailDto setComponent(ComponentDto componentDto, Component component) {
        LOG.trace("setComponent");

        Optional.ofNullable(componentDto.width()).ifPresent(component::setWidth);
        Optional.ofNullable(componentDto.height()).ifPresent(component::setHeight);
        Optional.ofNullable(componentDto.column()).ifPresent(component::setColumn);
        Optional.ofNullable(componentDto.row()).ifPresent(component::setRow);

        if (componentDto.parentId() == null && componentRepository.getParentId(component.getId()) == null) {
            component.setWidth(0L);
            component.setHeight(0L);
            component.setColumn(0L);
            component.setRow(0L);
            component.setOwnerId(userService.getUser().getId());
        } else if (component.getOwnerId() == null) {
            Component parent = componentRepository.findById(componentDto.parentId())
                .orElseThrow(() -> new NotFoundException("Parent not found"));
            component.setOwnerId(parent.getOwnerId());
        }

        component = componentRepository.save(component);

        if (componentDto.parentId() != null) {
            componentRepository.unlink(component.getId());
            componentRepository.link(componentDto.parentId(), component.getId());
        }

        Component rootComponent = getRootComponent(component);
        fireComponentUpdateEvent(rootComponent == component ? componentDto.parentId() : rootComponent.getId(), component.getId());

        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }

        return componentRepository.findById(component.getId())
            .orElseThrow(() -> new NotFoundException("Error component can not be set"))
            .accept(MappingDepth.DEEP);
    }

    @Override
    @Transactional
    public ComponentDetailDto getComponentById(long id) {
        LOG.trace("getBoard()");
        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("The requested component does not exist"));

        isUserAuthorizedToRead(getRootComponent(component));
        return component.accept(MappingDepth.DEEP);
    }

    @Override
    @Transactional
    public List<ComponentDetailDto> getRootComponents() {
        LOG.trace("getRootComponents()");
        return componentRepository.getComponentIdsByOwnerId(userService.getUser().getId()).stream()
            .map(id -> componentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("This should not happen")))
            .map(component -> component.accept(MappingDepth.SHALLOW)).toList();
    }

    @Override
    @Transactional
    public ComponentDetailDto updateComponent(ComponentUpdateDto dto) {
        LOG.trace("updateComponent({})", dto);

        List<String> errors = new ArrayList<>(componentValidator.validateComponent(dto, dto.id()));

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for updating an Component", errors);
        }

        Component component = componentRepository.findById(dto.id())
            .orElseThrow(() -> new NotFoundException("Component with given ID does not exist"));

        return setComponent(dto, component);
    }

    @Override
    @Transactional
    public void deleteComponent(Long id) {
        LOG.trace("deleteComponent({})", id);

        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Board with given ID does not exist"));

        isUserAuthorizedToWrite(getRootComponent(component), userService.getUser());

        if (component instanceof Image) {
            File file = new File(imagePath, String.valueOf(id));
            file.delete();
        }

        if (component instanceof MyCalendar) {
            File file = new File(calendarPath, String.valueOf(id) + ".ics");
            file.delete();
        }

        fireComponentDeleteEvent(getRootComponent(component).getId(), component.getId());
        componentRepository.deleteById(id);
    }

    @Override
    public Component getRootComponent(Component component) {
        Component rootComponent = component;
        while (!rootComponent.getParents().isEmpty()) {
            rootComponent = rootComponent.getParents().getFirst();
        }
        return rootComponent;
    }

    public void fireComponentUpdateEvent(Long rootId, Long selfId) {
        LOG.info("Sending update message to listening clients");

        var dto = new ComponentUpdateWsDto(rootId, selfId, ComponentUpdateWsType.changed);
        updateNotifier.notifyComponentUpdate(dto);
    }

    public void fireComponentDeleteEvent(Long rootId, Long selfId) {
        LOG.info("Sending delete message to listening clients");

        var dto = new ComponentUpdateWsDto(rootId, selfId, ComponentUpdateWsType.deleted);
        updateNotifier.notifyComponentUpdate(dto);
    }

    private void isUserAuthorizedToWrite(Component component, ApplicationUser user) {
        if (component.getOwnerId().equals(user.getId())) {
            return;
        }

        for (Permission permission : permissionRepository.findByComponent_Id(component.getId())) {
            if (permission.isWrite() && permission.getGroup().getMembers().contains(user)) {
                return;
            }
        }

        throw new UserNotAuthorizedException("User is not authorized to delete component");
    }

    private void isUserAuthorizedToRead(Component component) {
        ApplicationUser user = userService.getUser();

        if (component.getOwnerId().equals(user.getId())) {
            return;
        }

        for (Permission permission : permissionRepository.findByComponent_Id(component.getId())) {
            if (permission.isRead() && permission.getGroup().getMembers().contains(user)) {
                return;
            }
        }

        throw new UserNotAuthorizedException("User is not authorized to delete component");
    }
}