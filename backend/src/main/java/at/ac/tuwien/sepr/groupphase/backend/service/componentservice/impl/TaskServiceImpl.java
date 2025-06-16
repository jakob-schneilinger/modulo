package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Task;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.validation.TaskValidator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final TaskValidator taskValidator;
    private final UserRepository userRepository;
    private final ComponentService componentService;

    @Autowired
    public TaskServiceImpl(ComponentRepository componentRepository, TaskValidator taskValidator,
                           UserRepository userRepository, ComponentService componentService) {
        this.componentRepository = componentRepository;
        this.taskValidator = taskValidator;
        this.userRepository = userRepository;
        this.componentService = componentService;
    }

    @Override
    public ComponentDetailDto createTask(TaskCreateDto taskDto) {
        LOG.debug("createTask({})", taskDto);
        long userId = getUserId();
        LOG.debug("userID({})", userId);
        taskValidator.validateComponent(taskDto, -1L);

        return setTaskComponent(taskDto, new Task(), userId);
    }

    @Override
    public ComponentDetailDto repeatTask(TaskUpdateDto task) {
        LOG.trace("repeatTask({},{})", task.id(), task.parentId());
        if (task.children() != null) {
            manageChildren(task.children(), task);
        }

        return setRepetition(task, getTaskForUpdate(task.id()), getUserId());
    }

    @Override
    public ComponentDetailDto updateTask(TaskUpdateDto task) {
        LOG.trace("updateTask({})", task);
        Task taskToUpdate = getTaskForUpdate(task.id());
        long userId = getUserId();
        taskValidator.validateTaskForUpdate(task, taskToUpdate, userId);
        componentRepository.unlink(task.id());
        return setTaskComponent(task, taskToUpdate, userId);
    }

    private ComponentDetailDto setTaskComponent(TaskDto taskDto, Task task, long userId) {

        if (taskDto.name() != null) {
            task.setTaskName(taskDto.name());
            task.setStartDate(taskDto.startDate());
            task.setEndDate(taskDto.endDate());
            task.setCompleted(taskDto.completed());
            task.setRepeatable(taskDto.repeating());
        }
        return componentService.setComponent(taskDto, task);
    }

    private ComponentDetailDto setRepetition(TaskUpdateDto taskDto, Task task, long userId) {

        if (taskDto.name() != null) {
            task.setTaskName(taskDto.name());
        }

        if (taskDto.repeating() && taskDto.startDate() != null && taskDto.endDate() != null && LocalDate.now().isAfter(task.getEndDate())) {
            Period diff = Period.between(task.getStartDate(), task.getEndDate());
            task.setStartDate(LocalDate.now());
            task.setEndDate(LocalDate.now().plus(diff));
            task.setCompleted(false);
        }

        return componentService.setComponent(taskDto, task);
    }

    private void manageChildren(List<ComponentDetailDto> children, TaskUpdateDto rootTask) {
        for (ComponentDetailDto child : children) {
            if (child instanceof TaskDetailDto) {
                Optional<Component> taskOp = componentRepository.findById(((TaskDetailDto) child).id());
                if (taskOp.isPresent()) {
                    Task task = ((Task) taskOp.get());
                    if (task.getStartDate() != null) {
                        Period diff = Period.between(rootTask.startDate(), task.getStartDate());
                        task.setStartDate(LocalDate.now().plus(diff));
                    }
                    if (task.getEndDate() != null) {
                        Period diff = Period.between(rootTask.startDate(), task.getEndDate());
                        task.setEndDate(LocalDate.now().plus(diff));
                    }
                    task.setCompleted(false);
                    componentRepository.save(task);
                }
                manageChildren(((TaskDetailDto) child).children(), rootTask);
            }
            if (child instanceof BoardDetailDto) {
                LOG.error("Task {} is not completed", ((BoardDetailDto) child).id());
                manageChildren(((BoardDetailDto) child).children(), rootTask);
            }
        }
    }

    private Task getTaskForUpdate(long id) {
        Task taskToUpdate = componentRepository.findById(id)
            .filter(c -> c instanceof Task)
            .map(c -> (Task) c)
            .orElseThrow(() -> new NotFoundException("Task not found: " + id));
        taskToUpdate.setId(id);
        return taskToUpdate;
    }

    private long getUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ApplicationUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        return user.getId();
    }
}
