package at.ac.tuwien.sepr.groupphase.backend.integrationtest.componentService;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Task;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.BoardService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest

public class TaskComponentTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ComponentRepository componentRepository;
    @Autowired
    TaskService taskService;
    @Autowired
    BoardService componentService;
    private ApplicationUser user;

    @Autowired
    private BoardService boardService;

    private TaskCreateDto createDto(LocalDate start, LocalDate end, boolean repeating, boolean completed, Long parentId) {
        return new TaskCreateDto("task-Create", parentId, 1L, 1L, 1L, 1L, start, end, completed, repeating);
    }

    @BeforeEach
    @Transactional
    void prepareSecurityContext() {
        if (user == null) {
            user = new ApplicationUser();
            user.setUsername("testUser01");
            user.setEmail("ah@ahhh.ah");
            user.setDisplayName("Test User");
            user = userRepository.save(user);
        } else {
            user = userRepository.findByUsername("testUser01").orElseThrow();
        }

        var auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

    }

    @Transactional
    @Test
    void createTask_persistsEntity() {
        TaskCreateDto dto = createDto(LocalDate.now(), LocalDate.now().plusDays(1), false, false, null);
        TaskDetailDto task = (TaskDetailDto) taskService.createTask(dto);
        assertThat(task.name()).isEqualTo("task-Create");
        assertThat(componentRepository.findById(task.id())).isPresent();
    }

    @Transactional
    @Test
    void updateTask_updatesExistingEntity() {
        BoardCreateDto root = new BoardCreateDto(
            "name", null, null, null, null, null, null
        );

        ComponentDetailDto detail = boardService.createBoard(root);

        TaskDetailDto taskParent = (TaskDetailDto) taskService
                .createTask(createDto(LocalDate.now(), LocalDate.now().plusDays(40), false, false, detail.id()));

        TaskDetailDto task = (TaskDetailDto) taskService
                .createTask(createDto(LocalDate.now(), LocalDate.now().plusDays(1), false, false, taskParent.id()));

        TaskUpdateDto taskUpdateDto = new TaskUpdateDto(task.id(), "newName", 1L, 1L, 1L, 1L, null,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), true, false, taskParent.id());

        TaskDetailDto updatedTask = (TaskDetailDto) taskService.updateTask(taskUpdateDto);
        assertEquals("newName", updatedTask.name());
        assertEquals(LocalDate.now().plusDays(1), updatedTask.startDate());
        assertEquals(LocalDate.now().plusDays(2), updatedTask.endDate());
        assertEquals(true, updatedTask.completed());
        assertThat(componentRepository.findById(updatedTask.id())).isPresent();

    }

    @Transactional
    @Test
    void repeatTask_noChildrenOnlyParent_returnsRepeatedParentTask() {
        ComponentDetailDto board = componentService.createBoard(new BoardCreateDto("new board", null, null, 1L, 1L, 2L, 2L));
        TaskDetailDto task = (TaskDetailDto) taskService
                .createTask(createDto(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), false, false, null));
        TaskUpdateDto taskUpdateDto = new TaskUpdateDto(task.id(), "newName", 1L, 1L, 1L, 1L, null,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true, true, board.id());

        TaskDetailDto repeatedTask = (TaskDetailDto) taskService.repeatTask(taskUpdateDto);
        assertEquals("newName", repeatedTask.name());
        assertEquals(LocalDate.now(), repeatedTask.startDate());
        assertEquals(LocalDate.now().plusDays(5), repeatedTask.endDate());
        assertEquals(false, repeatedTask.completed());
        assertThat(componentRepository.findById(repeatedTask.id())).isPresent();
        assertThat(componentRepository.findById(task.id())).isPresent();

    }

    @Transactional
    @Test
    void repeatTask_withChildren_returnsRepeatedParentTask() {
        BoardCreateDto root = new BoardCreateDto(
            "name", null, null, null, null, null, null
        );

        ComponentDetailDto detail = boardService.createBoard(root);

        ComponentDetailDto board = componentService.createBoard(new BoardCreateDto("new board", null, null, 1L, 1L, 2L, 2L));
        TaskDetailDto task = (TaskDetailDto) taskService
                .createTask(createDto(LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true, true, board.id()));
        TaskDetailDto child = (TaskDetailDto) taskService
                .createTask(createDto(LocalDate.now().minusDays(7), LocalDate.now().minusDays(6), false, true, detail.id()));
        TaskUpdateDto taskChildDto = new TaskUpdateDto(child.id(), "newName", 1L, 1L, 1L, 1L, null,
                LocalDate.now().minusDays(7), LocalDate.now().minusDays(6), true, false, task.id());

        ComponentDetailDto childDetailDto = taskService.updateTask(taskChildDto);
        TaskUpdateDto taskParentDto = new TaskUpdateDto(task.id(), "parent", 1L, 1L, 1L, 1L, List.of(childDetailDto),
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), true, true, board.id());

        TaskDetailDto repeatedTask = (TaskDetailDto) taskService.repeatTask(taskParentDto);
        assertEquals("parent", repeatedTask.name());
        assertEquals(LocalDate.now(), repeatedTask.startDate());
        assertEquals(LocalDate.now().plusDays(5), repeatedTask.endDate());

        assertThat(componentRepository.findById(taskChildDto.id()).isPresent());
        Task childTask = (Task) componentRepository.findById(taskChildDto.id()).get();
        assertEquals(false, childTask.isCompleted());

    }

}
