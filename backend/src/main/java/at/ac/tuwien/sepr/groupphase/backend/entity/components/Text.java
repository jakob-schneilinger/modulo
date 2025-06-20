package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.service.ComponentStorageService;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a text component entity.
 */
@Entity
@Setter
@Getter
@Table(name = "text_content")
@DiscriminatorValue("text")
@PrimaryKeyJoinColumn(name = "id")

public class Text extends Component {

    /**
     * Content of ths text component.
     */
    private String content;

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this);
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