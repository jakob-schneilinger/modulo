package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupBoardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.PermissionDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.PermissionId;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupBoardService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.impl.GroupBoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GroupBoardServiceUnitTest {

    private ApplicationUser owner;
    private ApplicationUser member;
    private Group group;
    private Component component;

    private GroupRepository groupRepository;
    private ComponentRepository componentRepository;
    private UserService userService;
    private PermissionRepository permissionRepository;

    private GroupBoardService service;

    @BeforeEach
    void setup() {
        owner = new ApplicationUser();
        owner.setId(1L);
        owner.setUsername("owner");

        member = new ApplicationUser();
        member.setId(2L);
        member.setUsername("member");

        group = new Group();
        group.setId(100L);
        group.setName("Test Group");
        group.setOwner(owner);
        group.setMembers(Set.of(owner, member));
        group.setPermissions(new HashSet<>());

        component = new Board();
        component.setId(200L);
        component.setOwnerId(owner.getId());

        groupRepository = mock(GroupRepository.class);
        componentRepository = mock(ComponentRepository.class);
        userService = mock(UserService.class);
        permissionRepository = mock(PermissionRepository.class);

        service = new GroupBoardServiceImpl(groupRepository, componentRepository, userService, permissionRepository);

        when(userService.getUser()).thenReturn(owner);
    }

    @Test
    void addGroupToNonexistentComponentFails() {
        when(componentRepository.findById(200L)).thenReturn(Optional.empty());
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), new PermissionDto(true, true));

        assertThatThrownBy(() -> service.addGroupToBoard(dto))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addGroupToBoardWithoutOwnershipFails() {
        group.setOwner(member); // current user is not owner
        when(componentRepository.findById(200L)).thenReturn(Optional.of(component));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(group));

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), new PermissionDto(true, true));

        assertThatThrownBy(() -> service.addGroupToBoard(dto))
            .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void addGroupToBoardWithWritePermissionSucceeds() {
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), new PermissionDto(true, true));

        service.addGroupToBoard(dto);

        verify(permissionRepository).save(argThat(permission ->
            permission.getGroup().equals(group) &&
                permission.getComponent().equals(component) &&
                permission.isRead() &&
                permission.isWrite()
        ));
    }

    @Test
    void removeGroupFromNonexistentComponentFails() {
        when(componentRepository.findById(component.getId())).thenReturn(Optional.empty());

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), null);

        assertThatThrownBy(() -> service.removeGroupFromBoard(dto))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeGroupFromBoardWithoutPermissionFails() {
        group.setOwner(member);
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), null);

        assertThatThrownBy(() -> service.removeGroupFromBoard(dto))
            .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void updatePermissionWithMissingPermissionFails() {
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), new PermissionDto(true, false));

        assertThatThrownBy(() -> service.updateBoardPermission(dto))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updatePermissionSuccess() {
        Permission permission = new Permission();
        permission.setId(new PermissionId(group.getId(), component.getId()));
        permission.setComponent(component);
        permission.setGroup(group);
        permission.setRead(true);
        permission.setWrite(false);

        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(permissionRepository.findById(any())).thenReturn(Optional.of(permission));

        var dto = new GroupBoardDto(group.getId(), group.getName(), component.getId(), new PermissionDto(true, true));
        service.updateBoardPermission(dto);

        assertThat(permission.isWrite()).isTrue();
        verify(permissionRepository).save(permission);
    }

    @Test
    void getGroupsByBoardIdFailsIfNotOwner() {
        component.setOwnerId(member.getId()); // logged-in user is not owner
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));

        assertThatThrownBy(() -> service.getGroupsByBoardId(component.getId()))
            .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void hasWritePermissionThrowsIfNoPermission() {
        when(userService.getUser()).thenReturn(member);
        component.setOwnerId(owner.getId()); // not member

        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findByMembersContaining(owner)).thenReturn(Set.of(group));
        group.setPermissions(Set.of()); // no permissions on this component

        assertThatThrownBy(() -> service.hasWritePermission(component.getId()))
            .isInstanceOf(UserNotAuthorizedException.class)
            .hasMessageContaining("not authorized");
    }

    @Test
    void hasWritePermissionReturnsTrueIfWritePermissionsExist() {
        when(userService.getUser()).thenReturn(member);
        component.setOwnerId(owner.getId()); // not member

        var permission = new Permission();
        permission.setComponent(component);
        permission.setGroup(group);
        permission.setWrite(true); // only read

        group.setPermissions(Set.of(permission));

        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findByMembersContaining(member)).thenReturn(Set.of(group));

        var result = service.hasWritePermission(component.getId());
        assertThat(result).isTrue();
    }

    @Test
    void hasWritePermissionReturnsFalseIfOnlyReadPermissionsExist() {
        when(userService.getUser()).thenReturn(member);
        component.setOwnerId(owner.getId()); // not member

        var permission = new Permission();
        permission.setComponent(component);
        permission.setGroup(group);
        permission.setWrite(false); // only read

        group.setPermissions(Set.of(permission));

        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        when(groupRepository.findByMembersContaining(member)).thenReturn(Set.of(group));

        var result = service.hasWritePermission(component.getId());
        assertThat(result).isFalse();
    }

    @Test
    void hasWritePermissionReturnsNullIfUserIsOwner() {
        when(componentRepository.findById(component.getId())).thenReturn(Optional.of(component));
        component.setOwnerId(owner.getId());

        Boolean result = service.hasWritePermission(component.getId());
        assertThat(result).isNull();
    }
}
