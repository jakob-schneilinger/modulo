package at.ac.tuwien.sepr.groupphase.backend.validation;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Task;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@org.springframework.stereotype.Component
public class TaskValidator extends ComponentValidator  {


    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;

    public TaskValidator(ComponentRepository componentRepository, UserService userService, PermissionRepository permissionRepository) {
        super(componentRepository, userService, permissionRepository);
        this.componentRepository = componentRepository;
    }

    public void validateForCalendar(TaskDetailDto taskUpdateDto) {
        LOG.trace("validateForCalendar({})", taskUpdateDto);
        List<String> errors = new ArrayList<>();

        if (taskUpdateDto.endDate() == null) {
            errors.add("due date is required");
        }

        if (!errors.isEmpty()) {
            throw new at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException("Validation for inserting a task into a calendar", errors);
        }
    }

    public void validateTaskForUpdate(TaskUpdateDto taskUpdateDto, Component task, long userId) {
        LOG.trace("validateTaskForUpdate({}, {}, {})", taskUpdateDto, task, userId);
        List<String> errors = new ArrayList<>();
        errors.addAll(validDateRange(taskUpdateDto));

        if (!errors.isEmpty()) {
            throw new at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException("Validation for updating board failed", errors);
        }
        super.validateComponent(taskUpdateDto, taskUpdateDto.id());
    }

    private List<String> validDateRange(TaskUpdateDto taskDto) {
        List<String> errors = new ArrayList<>();
        if (taskDto.startDate() != null && taskDto.endDate() != null && taskDto.startDate().isAfter(taskDto.endDate())) {
            errors.add("Task start date is after end date");
        }
        Optional<Component> parentComponentOpt = componentRepository.findById(taskDto.parentId());
        if (parentComponentOpt.isEmpty()) {
            throw new NotFoundException("Parent with given ID does not exist");
        }
        LocalDate tightestEndDate;
        while (parentComponentOpt.isPresent()) {
            Component parentComponent = parentComponentOpt.get();
            if (parentComponent instanceof Task) {

                tightestEndDate = ((Task) parentComponent).getEndDate();

                if (tightestEndDate != null && taskDto.startDate() != null && taskDto.startDate().isAfter(tightestEndDate)) {
                    errors.add("Task start date is after parent task end date");
                }
                if (tightestEndDate != null && taskDto.endDate() != null && taskDto.endDate().isAfter(tightestEndDate)) {
                    errors.add("Task end date is after parent task end date");
                }
                if (((Task) parentComponent).isRepeatable() && taskDto.repeating()) {
                    errors.add("Only Parent task can be repeatable");
                }

            }

            parentComponentOpt = componentRepository.findByChildren_Id(parentComponent.getId());
        }
        return errors;
    }
}
