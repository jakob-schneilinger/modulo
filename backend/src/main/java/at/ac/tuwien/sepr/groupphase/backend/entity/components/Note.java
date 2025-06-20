package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.service.ComponentStorageService;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Represents a note component entity.
 */
@Entity
@Getter
@Setter
@Table(name = "note_content")
@DiscriminatorValue("note")
@PrimaryKeyJoinColumn(name = "id")
public class Note extends Component {

    /**
     * Title of this note component.
     */
    private String title;

    /**
     * Labels of this note component.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "label_component",
        joinColumns = @JoinColumn(name = "component_id"),
        inverseJoinColumns = @JoinColumn(name = "label_name")
    )
    private Set<Label> labels;

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this, MappingDepth.DEEP);
    }

    @Override
    public Long copyComponent(Long parentId, Boolean isTemplate, Long friendId, ComponentStorageService componentStorageService) {
        return componentStorageService.copyComponent(this, parentId, friendId, isTemplate);
    }

    @Override
    public void deleteComponent(ComponentStorageService componentStorageService) {
        componentStorageService.deleteComponent(this);
    }

    @Override
    public boolean isContainer() {
        return true;
    }
}