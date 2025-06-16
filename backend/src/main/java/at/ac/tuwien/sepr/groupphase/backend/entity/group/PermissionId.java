package at.ac.tuwien.sepr.groupphase.backend.entity.group;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {
    private long groupId;
    private long componentId;

    // equals and hashCode unbedingt überschreiben!
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
