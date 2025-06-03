package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Image;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.validation.ComponentValidator;
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
import java.util.Objects;
import java.util.Optional;

@Service
public class ComponentServiceImpl implements ComponentService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final UserService userService;
    private final ComponentValidator componentValidator;

    @Value("${global.location.image}")
    private String imagePath;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, UserService userService, ComponentValidator validator) {
        this.componentRepository = componentRepository;
        this.userService = userService;
        this.componentValidator = validator;
    }

    @Transactional
    public ComponentDetailDto setComponent(ComponentDto componentDto, Component component) {
        LOG.trace("setComponent");

        Optional.ofNullable(componentDto.width()).ifPresent(component::setWidth);
        Optional.ofNullable(componentDto.height()).ifPresent(component::setHeight);
        Optional.ofNullable(componentDto.column()).ifPresent(component::setColumn);
        Optional.ofNullable(componentDto.row()).ifPresent(component::setRow);
        component.setOwnerId(userService.getUserId());

        if (componentRepository.getParentId(component.getId()) == null && componentDto.parentId() == null) {
            component.setWidth(0L);
            component.setHeight(0L);
            component.setColumn(0L);
            component.setRow(0L);
        }
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
        return componentRepository.findById(id)
            .map(component -> component.accept(MappingDepth.DEEP))
            .orElseThrow(() -> new NotFoundException("The requested component does not exist"));
    }

    @Override
    public List<ComponentDetailDto> getRootComponents() {
        LOG.trace("getRootComponents()");
        return componentRepository.getComponentIdsByOwnerId(userService.getUserId()).stream()
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
    public void deleteComponent(Long id) {
        LOG.trace("deleteComponent({})", id);

        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Board with given ID does not exist"));

        if (!Objects.equals(component.getOwnerId(), userService.getUserId())) {
            throw new UserNotAuthorizedException("You are not authorized to delete this component");
        }

        if (component instanceof Image) {
            File file = new File(imagePath, String.valueOf(id));
            file.delete();
        }

        componentRepository.deleteById(id);
    }
}