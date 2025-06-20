package at.ac.tuwien.sepr.groupphase.backend.entity.group;

import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a permission entity.
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {

    /**
     * ID of this permission.
     */
    @EmbeddedId
    private PermissionId id = new PermissionId();

    /**
     * Group associated with this permission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    /**
     * Component associated with this permission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("componentId")
    @JoinColumn(name = "component_id")
    private Component component;

    /**
     * Read authorization of this permission.
     */
    private boolean read;

    /**
     * Write authorization of this permission.
     */
    private boolean write;
}