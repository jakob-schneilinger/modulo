package at.ac.tuwien.sepr.groupphase.backend.service.impl;




import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;
import at.ac.tuwien.sepr.groupphase.backend.service.ComponentService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class ComponentServiceImpl implements ComponentService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentValidator componentValidator;
    private final UserRepository userRepository;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository, ComponentValidator componentValidator, JwtUtils jwtUtils, UserRepository userRepository) {
        this.componentRepository = componentRepository;
        this.componentValidator = componentValidator;
        this.userRepository = userRepository;
    }

    @Override
    public ComponentDetailDto createBoard(BoardCreateDto boardDto) {
        LOG.trace("createBoard({})", boardDto);
        long userId = getUserId();
        componentValidator.validateBoardForCreation(boardDto, userId);

        return setBoardComponent(boardDto, new Board(), userId);
    }

    @Override
    public ComponentDetailDto createTextComponent(TextCreateDto dto) {
        LOG.trace("createText({})", dto);

        Text text = new Text();
        text.setWidth(dto.width());
        text.setText(dto.text());
        text.setFontSize(dto.fontSize());
        text.setOwnerId(getUserId());

        return setComponent(dto, text, getUserId());

    }

    @Override
    public ComponentDetailDto updateTextComponent(TextUpdateDto dto) {
        LOG.trace("updateText({})", dto);
        LOG.warn("{} the id of the text", dto.id());

        Text text = componentRepository.findById(dto.id())
            .filter(c -> c instanceof Text)
            .map(c -> (Text) c)
            .orElseThrow(() -> new NotFoundException("Text not found: " + dto.id()));

        if (!text.getOwnerId().equals(getUserId())) {
            throw new RuntimeException("User is not owner of this component");
        }

        text.setText(dto.text());
        text.setFontSize(dto.fontSize());

        if (dto.parentId() != null) {
            componentRepository.unlink(dto.id());
            componentRepository.link(dto.parentId(), dto.id());
        }

        return setComponent(dto, text, getUserId());
    }

    @Override
    public ComponentDetailDto updateBoard(BoardUpdateDto boardDto) {
        LOG.trace("updateBoard({})", boardDto);
        Optional<Component> optionalComponent = componentRepository.findById(boardDto.id());
        Component component;

        if (optionalComponent.isPresent()) {
            component = optionalComponent.get();
        } else {
            throw new NotFoundException("Board with given ID does not exist");
        }

        if (!(component instanceof Board board)) {
            throw new ConflictException("Failed to update board", List.of("Id given does not reference a component with the type board"));
        }

        long userId = getUserId();
        componentValidator.validateBoardForUpdate(boardDto, component, userId);
        componentRepository.unlink(boardDto.id());
        return setBoardComponent(boardDto, board, userId);
    }

    private ComponentDetailDto setBoardComponent(BoardDto boardDto, Board board, long userId) {
        if (boardDto.name() != null) {
            board.setBoardName(boardDto.name());
        }
        return setComponent(boardDto, board, userId);
    }

    private ComponentDetailDto setComponent(ComponentDto componentDto, Component component, long userId) {
        component.setWidth(componentDto.width());
        component.setHeight(componentDto.height());
        component.setColumn(componentDto.column());
        component.setRow(componentDto.row());
        component.setOwnerId(userId);

        component = componentRepository.save(component);

        if (componentDto.parentId() != null) {
            componentRepository.link(componentDto.parentId(), component.getId());
        }

        return component.accept(MappingDepth.DEEP); //Should it be DEEP?
    }

    @Override
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
            .map(optionalComponent -> optionalComponent.orElseThrow(() -> new NotFoundException("This should not happen")))
            .map(component -> component.accept(MappingDepth.SHALLOW)).toList();
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

        if (!Objects.equals(component.getOwnerId(), getUserId())) {
            throw new UserNotAuthorizedException("You are not authorized to delete this component");
        }
        componentRepository.deleteById(id);
    }

    private long getUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ApplicationUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found" + username));
        return user.getId();
    }
}
