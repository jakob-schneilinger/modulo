package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskUpdateDto;

public interface TaskService {

    /**
     * Updates an existing Task in the database.
     *
     * @param task the task to be updated, containing the updated values
     * @return Component detail of the updated Task, including all its properties
     */
    ComponentDetailDto updateTask(TaskUpdateDto task);

    /**
     * Creates a Task in Database.
     *
     * @param task the task to be persisted
     * @return Component detail of the created Task
     */
    ComponentDetailDto createTask(TaskCreateDto task);

    /**
     * Repeats an existing Task based on the provided TaskUpdateDto.
     * The task will be replicated while preserving the necessary details specified in the input.
     *
     * @param task the task to be repeated, containing all required details
     * @return ComponentDetailDto containing the details of the repeated Task
     */
    ComponentDetailDto repeatTask(TaskUpdateDto task);

}
