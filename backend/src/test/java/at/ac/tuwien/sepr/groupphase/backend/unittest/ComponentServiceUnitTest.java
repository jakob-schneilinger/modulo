package at.ac.tuwien.sepr.groupphase.backend.unittest;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl.ComponentServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl.ComponentUpdateNotifier;
import at.ac.tuwien.sepr.groupphase.backend.validation.ComponentValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentServiceUnitTest {


    @Test
    void contextLoads(){
        new ComponentServiceImpl(null, null, null, null, null);
    }


    // should behave the same for all components
    @Test
    void setComponentForBoard() {
        // mock the dependencies

        // mock UpdateNotifier
        ComponentUpdateNotifier notifier = mock(ComponentUpdateNotifier.class);

        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        Board initial = initialBoard();
        Board changed = changedBoard();

        when(componentRepository.save((Board) any())).thenReturn(changed);
        when(componentRepository.getParentId(initial.getId())).thenReturn(null);
        when(componentRepository.findById(changed.getId())).thenReturn(Optional.of(changed)); // <-- wichtig!

        UserService userService = mockUserService();

        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, null, notifier, null);

        ComponentDetailDto reference = changedBoardDetailDto();
        ComponentDetailDto updated = componentService.setComponent(changedBoardDto(), initial);

        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(reference.id());
        assertThat(((BoardDetailDto) updated).name()).isEqualTo(((BoardDetailDto) reference).name());
        assertThat(updated.width()).isEqualTo(reference.width());
    }

    @Test
    void getComponentByIdFindsComponent(){
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        Board initial = initialBoard();
        when(componentRepository.findById(initial.getId())).thenReturn(Optional.of(initial));
        UserService userService = mockUserService();

        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, null, null, null);
        ComponentDetailDto componentById = componentService.getComponentById(initial.getId());

        assertThat(componentById).isNotNull();
        assertThat(((BoardDetailDto)componentById).name()).isEqualTo(initial.getBoardName());
        assertThat(componentById.id()).isEqualTo(initial.getId());
        assertThat(componentById.width()).isEqualTo(initial.getWidth());
    }

    @Test
    void getComponentByIdThrowsNotFound(){
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        Board initial = initialBoard();
        when(componentRepository.findById(initial.getId())).thenReturn(Optional.empty());

        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, null, null, null, null);
        assertThatThrownBy(() -> componentService.getComponentById(initial.getId())).isInstanceOf(NotFoundException.class);
    }


    @Test
    void getRootComponentsReturnsRoots(){
        // mock user service
        ApplicationUser user = testUser();
        UserService userService = mockUserService();
        // mock component repository
        Board component = initialBoard();
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        when(componentRepository.getComponentIdsByOwnerId(user.getId())).thenReturn(List.of(component.getId()));
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));

        // initialize unit with mocked dependencies
        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, null, null, null);
        // perform tested operation
        List<ComponentDetailDto> roots = componentService.getRootComponents();

        // assert correctness
        assertThat(roots).isNotNull();
        assertThat(roots.size()).isEqualTo(1);

        ComponentDetailDto root = roots.getLast();
        assertThat(root).isNotNull();
        assertThat(root.id()).isEqualTo(component.getId());
        assertThat(root.width()).isEqualTo(component.getWidth());
        assertThat(((BoardDetailDto)root).name()).isEqualTo(component.getBoardName());
    }

    @Test
    void updateComponentForBoard(){
        // mock user service
        ApplicationUser user = testUser();
        UserService userService = mockUserService();

        // mock UpdateNotifier
        ComponentUpdateNotifier notifier = mock(ComponentUpdateNotifier.class);

        // mock component repository
        Board component = initialBoard();
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        Board changed = changedBoard();
        when(componentRepository.save((Board)any())).thenReturn(changed);
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));

        // mock validation
        ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
        ComponentUpdateDto updateDto = updateComponentDto();
        when(componentValidator.validateComponent(updateDto, user.getId())).thenReturn(new ArrayList<>());

        // initialize mocked unit
        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, componentValidator, notifier, null);
        // perform tested operation
        ComponentUpdateDto transformedUpdateDto = new ComponentUpdateDto(
            updateDto.id(),
            updateDto.parentId(),
            updateDto.width(),
            updateDto.height(),
            updateDto.column(),
            updateDto.row()
        );
        ComponentDetailDto updated = componentService.updateComponent(transformedUpdateDto);

        // assert correctness
        assertThat(updated).isNotNull();
        assertThat(updated.id()).isEqualTo(updateDto.id());
        assertThat(updated.width()).isEqualTo(updateDto.width());
    }

    @Test
    void updateComponentThrowsNotFound(){
        // mock user service
        ApplicationUser user = testUser();
        UserService userService = mockUserService();
        // mock component repository
        Board component = initialBoard();
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        // repository returns empty optional
        when(componentRepository.findById(component.getId())).thenReturn(Optional.empty());

        // mock validation
        ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
        ComponentUpdateDto updateDto = updateComponentDto();
        when(componentValidator.validateComponent(updateDto, user.getId())).thenReturn(new ArrayList<>());

        // initialize mocked unit
        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, componentValidator, null, null);
        // perform tested operation
        ComponentUpdateDto transformedUpdateDto = new ComponentUpdateDto(
            updateDto.id(),
            updateDto.parentId(),
            updateDto.width(),
            updateDto.height(),
            updateDto.column(),
            updateDto.row()
        );
        assertThatThrownBy(() -> componentService.updateComponent(transformedUpdateDto)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateComponentThrowsValidation(){
        // mock user service
        ApplicationUser user = testUser();
        UserService userService = mockUserService();

        // mock component repository
        Board component = initialBoard();
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        Board changed = changedBoard();
        when(componentRepository.save(changed)).thenReturn(changed);
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));

        // mock validation
        ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
        ComponentUpdateDto updateDto = updateComponentDto();
        //set validation to fail
        when(componentValidator.validateComponent(updateDto, user.getId())).thenReturn(List.of("error!"));

        // initialize mocked unit
        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, componentValidator, null, null);
        // perform tested operation
        ComponentUpdateDto transformedUpdateDto = new ComponentUpdateDto(
            updateDto.id(),
            updateDto.parentId(),
            updateDto.width(),
            updateDto.height(),
            updateDto.column(),
            updateDto.row()
        );
        assertThatThrownBy(() -> componentService.updateComponent(transformedUpdateDto)).isInstanceOf(ValidationException.class);
    }

    @Test
    void deleteComponentForBoard(){
        // mock user service
        UserService userService = mockUserService();

        // mock UpdateNotifier
        ComponentUpdateNotifier notifier = mock(ComponentUpdateNotifier.class);

        // mock component repository
        Board component = initialBoard();
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        // void methods do not need to be mocked (like delete)
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));

        // initialize mocked unit
        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, null, notifier, null);

        assertDoesNotThrow(() -> componentService.deleteComponent(initialBoard().getId()));
    }

    @Test
    void deleteComponentThrowsNotFound(){
        // mock user service
        UserService userService = mockUserService();
        // mock component repository
        Board component = initialBoard();
        ComponentRepository componentRepository = Mockito.mock(ComponentRepository.class);
        // repository returns empty optional
        when(componentRepository.findById(component.getId())).thenReturn(Optional.empty());

        // initialize mocked unit
        ComponentServiceImpl componentService = new ComponentServiceImpl(componentRepository, userService, null, null, null);

        assertThatThrownBy(() -> componentService.deleteComponent(initialBoard().getId())).isInstanceOf(NotFoundException.class);
    }

    private Board initialBoard(){
        Board board = new Board();
        board.setBoardName("boardname");
        board.setId(1L);
        board.setWidth(1L);
        board.setHeight(1L);
        board.setRow(1L);
        board.setColumn(1L);
        board.setOwnerId(1L);
        board.setChildren(new ArrayList<>());
        return board;
    }

    private Board changedBoard(){
        Board board = new Board();
        board.setBoardName("boardname");
        board.setId(1L);
        board.setWidth(100L);
        board.setHeight(1L);
        board.setRow(1L);
        board.setColumn(1L);
        board.setOwnerId(1L);
        board.setChildren(new ArrayList<>());
        return board;
    }

    private ComponentUpdateDto updateComponentDto(){
        return new ComponentUpdateDto(1, 1L, 100L, 1L, 1L, 1L);
    }

    private ComponentDetailDto initialBoardDetailDto(){
        return new BoardDetailDto(1, null, "boardname", 5, 1, 1, 1, 1, null);
    }

    private ComponentDto changedBoardDto(){
        return new BoardUpdateDto(1, "boardname", 5, 1L, 100L, 1L, 1L, 1L);
    }
    private ComponentDetailDto changedBoardDetailDto(){
        return new BoardDetailDto(1, null, "boardname", 5, 100, 1, 1, 1, null);
    }

    private ApplicationUser testUser(){
       ApplicationUser user = new ApplicationUser("test-username", "test@email.com", "12345678");
       user.setId(1L);
       return user;
    }

    private UserRepository mockUserRepositoryWithSecurityContext(){
        // set context holder with mocked context
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = Mockito.mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // set mocked user to return from authentication
        ApplicationUser user = testUser();
        when(authentication.getName()).thenReturn(user.getUsername());

        // mock user repository
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));

        return userRepository;
    }

    private UserService mockUserService(){
        UserService userService = Mockito.mock(UserService.class);
        when(userService.getUser()).thenReturn(testUser());
        return userService;
    }
}
