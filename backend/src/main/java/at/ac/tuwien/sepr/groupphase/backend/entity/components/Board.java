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

/**
 * Represents a board component entity.
 */
@Entity
@Getter
@Setter
@Table(name = "board_content")
@DiscriminatorValue("board")
@PrimaryKeyJoinColumn(name = "id")
public class Board extends Component {

    /**
     * Name of this board component.
     */
    @Column(name = "board_name")
    private String boardName;

    /**
     * Maximum depth of this board component (only used in root boards).
     */
    @Column(name = "max_depth")
    private Integer depth;

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

    @Override
    public boolean isContainer() {
        return true;
    }
}