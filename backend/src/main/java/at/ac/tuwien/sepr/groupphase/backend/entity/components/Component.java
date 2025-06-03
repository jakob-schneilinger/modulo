package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.FetchType;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "components")
public abstract class Component {

    public abstract ComponentDetailDto accept(MappingDepth depth);

    public boolean isContainer() {
        return false;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "component_column")
    private Long column;

    @Column(name = "component_row")
    private Long row;

    private Long width;

    private Long height;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
        name = "container_children",
        joinColumns = @JoinColumn(name = "id_container"),
        inverseJoinColumns = @JoinColumn(name = "id_child")
    )
    private List<Component> children = new ArrayList<>();

}