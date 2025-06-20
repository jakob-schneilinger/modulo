package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.service.ComponentStorageService;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents a task component entity.
 */
@Entity
@Table(name = "task_content")
@DiscriminatorValue("task")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "id")
public class Task extends Component {

    /**
     * Name of this task component.
     */
    @Column(name = "task_name")
    private String taskName;

    /**
     * Start date of this task component.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * End date of this task component.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Completeness of this task.
     */
    private boolean completed;

    /**
     * Repeatability of this task.
     */
    private boolean repeatable;

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this, depth);
    }

    @Override
    public Long copyComponent(Long parentId, Boolean isTemplate, Long friendId, ComponentStorageService componentStorageService) {
        return componentStorageService.copyComponent(this, parentId, friendId, isTemplate);
    }

    @Override
    public void deleteComponent(ComponentStorageService componentStorageService) {
        componentStorageService.deleteComponent(this);
    }
}