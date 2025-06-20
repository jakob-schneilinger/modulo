package at.ac.tuwien.sepr.groupphase.backend.entity.group;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the permission id entity.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {

    /**
     * Group part of the id.
     */
    private long groupId;

    /**
     * Component part of the id.
     */
    private long componentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PermissionId)) {
            return false;
        }
        PermissionId that = (PermissionId) o;
        return Objects.equals(groupId, that.groupId)
            && Objects.equals(componentId, that.componentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, componentId);
    }
}