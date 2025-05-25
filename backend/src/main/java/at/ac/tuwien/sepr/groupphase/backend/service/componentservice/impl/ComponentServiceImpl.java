package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Image;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.validation.ComponentValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ComponentServiceImpl implements ComponentService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final UserRepository userRepository;
    private final ComponentValidator componentValidator;

    @Value("${global.location.image}")
    private String imagePath;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, UserRepository userRepository, ComponentValidator validator) {
        this.componentRepository = componentRepository;
        this.userRepository = userRepository;
        this.componentValidator = validator;
    }

    @Transactional
    public ComponentDetailDto setComponent(ComponentDto componentDto, Component component, long userId) {
        component.setWidth(componentDto.width());
        component.setHeight(componentDto.height());
        component.setColumn(componentDto.column());
        component.setRow(componentDto.row());
        component.setOwnerId(userId);
        component = componentRepository.save(component);

        if (componentDto.parentId() != null) {
            componentRepository.unlink(component.getId());
            componentRepository.link(componentDto.parentId(), component.getId());
        }

        return component.accept(MappingDepth.DEEP);
    }

    @Override
    @Transactional
    public ComponentDetailDto getComponentById(long id) {
        LOG.trace("getBoard()");
        Optional<Component> components = componentRepository.findById(id);

        if (components.isPresent()) {
            Component component = components.get();
            return component.accept(MappingDepth.DEEP);
        } else {
            throw new NotFoundException("The requested component does not exist");
        }
    }

    @Override
    public List<ComponentDetailDto> getRootComponents() {
        LOG.trace("getRootComponents()");
        return componentRepository.getComponentIdsByOwnerId(getUserId()).stream()
                .map(componentRepository::findById)
                .map(optionalComponent -> optionalComponent
                        .orElseThrow(() -> new NotFoundException("This should not happen")))
                .map(component -> component.accept(MappingDepth.SHALLOW)).toList();
    }

    @Override
    @Transactional
    public ComponentDetailDto updateComponent(ComponentUpdateDto dto) {
        LOG.trace("updateComponent({})", dto);

        Optional<Component> optionalComponent = componentRepository.findById(dto.id());
        Component component;

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else {
            throw new NotFoundException("Component with given ID does not exist");
        }

        List<String> errors = new ArrayList<>();

        if (component.isContainer()) {
            errors.addAll(componentValidator.validateContainerForUpdate(dto, component));
        }

        long userId = getUserId();

        errors.addAll(componentValidator.validateComponent(dto, userId, dto.id()));

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for updating an Component", errors);
        }

        return setComponent(dto, component, userId);
    }

    @Override
    public void deleteComponent(Long id) {
        LOG.trace("deleteComponent({})", id);
        Optional<Component> optionalComponent = componentRepository.findById(id);
        Component component;

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else {
            throw new NotFoundException("Board with given ID does not exist");
        }

        if (component instanceof Image) {
            File file = new File(imagePath, String.valueOf(id));
            file.delete();
        }

        if (!Objects.equals(component.getOwnerId(), getUserId())) {
            throw new UserNotAuthorizedException("You are not authorized to delete this component");
        }
        componentRepository.deleteById(id);
    }

    public long getUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ApplicationUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
        return user.getId();
    }
}
