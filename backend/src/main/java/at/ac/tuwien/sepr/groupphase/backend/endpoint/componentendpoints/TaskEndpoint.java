package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TaskService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing component operations.
 * This controller provides endpoints for creating, updating, retrieving, and deleting components.
 * Specifically, it includes operations for managing boards within the application.
 */
@RestController
@RequestMapping(value = "/api/v1/component")
public class TaskEndpoint {

    private final TaskService service;

    @Autowired
    public TaskEndpoint(TaskService service) {
        this.service = service;
    }

    /**
     * Creates a Task in the database.
     *
     * @param dto the task to be created
     * @return component detail of the task
     */
    @PermitAll
    @PostMapping("/task")
    public ResponseEntity<ComponentDetailDto> createTask(
        @RequestBody TaskCreateDto dto) {
        return new ResponseEntity<>(service.createTask(dto), HttpStatus.CREATED);
    }

    /**
     * Updates a Task in the database.
     *
     * @param dto the task to be updated
     * @return component detail of the task
     */
    @PermitAll
    @PutMapping("/task")
    public ResponseEntity<ComponentDetailDto> updateTask(
        @RequestBody TaskUpdateDto dto) {
        return new ResponseEntity<>(service.updateTask(dto), HttpStatus.OK);
    }

    /**
     * Repeats a task in the database.
     *
     * @param dto the task to repeat
     * @return component detail of the task
     */
    @PermitAll
    @PutMapping("/task/repeat")
    public ResponseEntity<ComponentDetailDto> repeatTask(
        @RequestBody TaskUpdateDto dto
    ) {
        return new ResponseEntity<>(service.repeatTask(dto), HttpStatus.OK);
    }

}
