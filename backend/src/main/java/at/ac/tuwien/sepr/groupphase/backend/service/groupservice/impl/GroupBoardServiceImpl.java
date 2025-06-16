package at.ac.tuwien.sepr.groupphase.backend.service.groupservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupBoardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.PermissionDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.PermissionId;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupBoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupBoardServiceImpl implements GroupBoardService {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final GroupRepository groupRepository;
    private final ComponentRepository componentRepository;
    private final UserService userService;
    private final PermissionRepository permissionRepository;

    public GroupBoardServiceImpl(GroupRepository groupRepository, ComponentRepository componentRepository, UserService userService, PermissionRepository permissionRepository) {
        this.groupRepository = groupRepository;
        this.componentRepository = componentRepository;
        this.userService = userService;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void addGroupToBoard(GroupBoardDto groupBoardDto) {
        LOG.trace("addGroupToBoard({})", groupBoardDto);
        Component component = componentRepository.findById(groupBoardDto.boardId())
            .orElseThrow(() -> new NotFoundException("Component not found"));

        Group group = groupRepository.findById(groupBoardDto.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found"));

        checkForOwner(group);

        Permission newPermission = new Permission();
        newPermission.setId(new PermissionId(group.getId(), component.getId()));
        newPermission.setComponent(component);
        newPermission.setGroup(group);
        newPermission.setRead(true);
        if (groupBoardDto.permission().write()) {
            newPermission.setWrite(true);
        }

        permissionRepository.save(newPermission);
    }

    @Override
    @Transactional
    public void removeGroupFromBoard(GroupBoardDto groupBoardDto) {
        LOG.trace("removeGroupFromBoard({})", groupBoardDto);
        Component component = componentRepository.findById(groupBoardDto.boardId())
            .orElseThrow(() -> new NotFoundException("Component not found"));

        Group group = groupRepository.findById(groupBoardDto.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found"));

        checkForOwner(group);

        PermissionId permissionId = new PermissionId(group.getId(), component.getId());
        permissionRepository.deleteById(permissionId);
    }

    @Override
    @Transactional
    public void updateBoardPermission(GroupBoardDto groupBoardDto) {
        LOG.trace("updateBoardPermission({})", groupBoardDto);
        Component component = componentRepository.findById(groupBoardDto.boardId())
            .orElseThrow(() -> new NotFoundException("Component not found"));

        Group group = groupRepository.findById(groupBoardDto.groupId())
            .orElseThrow(() -> new NotFoundException("Group not found"));

        checkForOwner(group);

        PermissionId permissionId = new PermissionId(group.getId(), component.getId());

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new NotFoundException("Permission not found"));

        permission.setWrite(groupBoardDto.permission().write());
        permissionRepository.save(permission);
    }

    @Override
    @Transactional
    public List<ComponentDetailDto> getGroupRoots() {
        LOG.trace("getGroupRoots()");
        ApplicationUser user = userService.getUser();
        Set<Group> groups = groupRepository.findByMembersContaining(user);

        return groups.stream().flatMap(group -> group.getPermissions().stream())
            .map(Permission::getComponent)
            .map(component -> component.accept(MappingDepth.SHALLOW))
            .distinct()
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<GroupBoardDto> getGroupsByBoardId(long id) {
        LOG.trace("getGroupsByBoardId({})", id);
        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Component not found"));

        ApplicationUser currentUser = userService.getUser();

        if (!Objects.equals(component.getOwnerId(), currentUser.getId())) {
            throw new UserNotAuthorizedException("You are not authorized to view this group");
        }

        Set<Group> ownedGroups = groupRepository.findByOwner(currentUser);

        Map<Long, Permission> permissionMap = permissionRepository.findByComponent_Id(component.getId()).stream()
            .collect(Collectors.toMap(
                p -> p.getGroup().getId(),
                p -> p
            ));

        return ownedGroups.stream()
            .map(group -> {
                Permission permission = permissionMap.get(group.getId());
                PermissionDto permissionDto = permission != null ? new PermissionDto(permission.isRead(), permission.isWrite()) : null;
                return new GroupBoardDto(group.getId(), group.getName(), component.getId(), permissionDto);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean hasWritePermission(long id) {
        LOG.trace("hasWritePermission({})", id);
        ApplicationUser user = userService.getUser();

        Component component = componentRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Component not found"));

        if (Objects.equals(component.getOwnerId(), user.getId())) {
            return null;
        }

        Set<Group> groups = groupRepository.findByMembersContaining(user);

        Set<Permission> permissions = groups.stream().flatMap(group -> group.getPermissions().stream())
            .filter(permission -> permission.getComponent().getId().equals(component.getId()))
            .collect(Collectors.toSet());

        if (permissions.isEmpty()) {
            throw new UserNotAuthorizedException("You are not authorized to view this board");
        }

        for (Permission permission : permissions) {
            if (permission.isWrite()) {
                return true;
            }
        }
        return false;
    }

    private void checkForOwner(Group group) {
        LOG.trace("checkForOwner({})", group);
        if (!group.getOwner().equals(userService.getUser())) {
            throw new UserNotAuthorizedException("Not owner for this group");
        }
    }
}
