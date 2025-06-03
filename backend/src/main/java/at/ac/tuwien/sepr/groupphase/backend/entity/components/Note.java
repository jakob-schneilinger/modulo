package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
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

@Entity
@Getter
@Setter
@Table(name = "note_content")
@DiscriminatorValue("note")
@PrimaryKeyJoinColumn(name = "id")
public class Note extends Component {

    private String title;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "label_component",
        joinColumns = @JoinColumn(name = "component_id"),
        inverseJoinColumns = @JoinColumn(name = "label_name")
    )
    private Set<Label> labels;

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this);
    }

    @Override
    public boolean isContainer() {
        return true;
    }
}
