package at.ac.tuwien.sepr.groupphase.backend.entity.group;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailWithMembersDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a group entity.
 */
@Entity
@Getter
@Setter
@Table(name = "groups")
public class Group {

    /**
     * ID of this group.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of this group.
     */
    @Column(name = "name")
    private String name;

    /**
     * Owner if this group.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private ApplicationUser owner;

    /**
     * Members of this group.
     */
    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<ApplicationUser> members;

    /**
     * Permissions of this group.
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Maps this group to a GroupDto.
     *
     * @return GroupDto of this group
     */
    public GroupDto getGroupDto() {
        return new GroupDto(id, name);
    }

    /**
     * Maps this group to a GroupDetailDto.
     *
     * @return GroupDetailDto of this group
     */
    public GroupDetailDto getGroupDetailDto() {
        return new GroupDetailDto(id, name, new UserDto(owner.getUsername(), owner.getDisplayName(), null));
    }

    /**
     * Maps this group to a GroupDetailWithMembersDto.
     *
     * @return GroupDetailWithMembersDto of this group
     */
    public GroupDetailWithMembersDto getGroupDetailWithMembersDto() {
        return new GroupDetailWithMembersDto(id, name, new UserDto(owner.getUsername(), owner.getDisplayName(), null),
            members.stream().map(member -> new UserDto(member.getUsername(), member.getDisplayName(), null)).collect(Collectors.toSet()));
    }
}