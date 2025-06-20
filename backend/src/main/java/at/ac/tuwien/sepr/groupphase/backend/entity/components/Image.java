package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represents an image component entity.
 */
@Entity
@DiscriminatorValue("image")
@Table(name = "image_content")
public class Image extends Component {

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this);
    }
}