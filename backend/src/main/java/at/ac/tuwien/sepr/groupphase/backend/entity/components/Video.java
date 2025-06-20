package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Represents a video component entity.
 */
@Entity
@DiscriminatorValue("video")
@Table(name = "video_content")
public class Video extends Component {

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this);
    }
}