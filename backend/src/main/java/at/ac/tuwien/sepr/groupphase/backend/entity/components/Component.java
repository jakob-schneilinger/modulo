package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.service.ComponentStorageService;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an abstract component entity which serves as a blueprint for all the over components.
 */
@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@Table(name = "components")
public abstract class Component {

    /**
     * Visitor method to map this component entity to its dto form.
     *
     * @param depth defines it should be mapped SHALLOW or DEEP
     * @return dto of this component entity
     */
    public abstract ComponentDetailDto accept(MappingDepth depth);

    /**
     * Copies this component entity.
     *
     * @param parentId parentId of the copied entity
     * @param isTemplate template identifier of the copied entity
     * @param friendId friend ID
     * @param componentStorageService storage service
     * @return id of copied entity or parentId when not supported
     */
    public Long copyComponent(Long parentId, Boolean isTemplate, Long friendId, ComponentStorageService componentStorageService) {
        return parentId;
    }

    /**
     * Deletes this component.
     *
     * @param componentStorageService storage service
     */
    public void deleteComponent(ComponentStorageService componentStorageService) {
    }

    /**
     * Checks if given component is a container.
     *
     * @return true if container, false if not
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * ID of the component.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Column position of this component.
     */
    @Column(name = "component_column")
    private Long column;

    /**
     * Row position of this component.
     */
    @Column(name = "component_row")
    private Long row;

    /**
     * Width position of this component.
     */
    private Long width;

    /**
     * Height position of this component.
     */
    private Long height;

    /**
     * Owner id of this component.
     */
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    /**
     * Template discriminator for checking if given component is a template or not.
     */
    @Column(name = "is_template", nullable = false)
    private Boolean isTemplate = false;

    /**
     * Children of this component (if it is a container).
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
        name = "container_children",
        joinColumns = @JoinColumn(name = "id_container"),
        inverseJoinColumns = @JoinColumn(name = "id_child")
    )
    private List<Component> children = new ArrayList<>();

    /**
     * Parents of this component (Should only be one).
     */
    @ManyToMany(mappedBy = "children", fetch = FetchType.LAZY)
    private List<Component> parents = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Component component = (Component) o;
        return Objects.equals(id, component.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}