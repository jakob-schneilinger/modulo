package at.ac.tuwien.sepr.groupphase.backend.entity.group;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupBoardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.PermissionDto;
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

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {

    @EmbeddedId
    private PermissionId id = new PermissionId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("componentId")
    @JoinColumn(name = "component_id")
    private Component component;

    private boolean read;
    private boolean write;

    public GroupBoardDto getGroupBoardDto() {
        return new GroupBoardDto(group.getId(), group.getName(), component.getId(), new PermissionDto(read, write));
    }
}
