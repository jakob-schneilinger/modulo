package at.ac.tuwien.sepr.groupphase.backend.service.groupservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailWithMembersDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public GroupServiceImpl(GroupRepository groupRepository, UserService userService, UserRepository userRepository, PermissionRepository permissionRepository) {
        this.groupRepository = groupRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public GroupDetailDto createGroup() {
        LOG.trace("createGroup");
        Group group = new Group();
        group.setName("Default Group");
        group.setOwner(userService.getUser());
        group.setMembers(Set.of(userService.getUser()));
        group = groupRepository.save(group);
        return group.getGroupDetailDto();
    }

    @Override
    @Transactional
    public GroupDetailDto updateGroupName(GroupDto dto) {
        LOG.trace("updateGroup({})", dto);

        if (dto.name() == null || dto.name().isEmpty()) {
            throw new ValidationException("Validation failed for updating Group", List.of("Name not provided"));
        }

        if (dto.name().length() > 30) {
            throw new ValidationException("Validation failed for updating Group", List.of("Name greater than 30 characters"));
        }

        Group group = groupRepository.findById(dto.id()).orElseThrow(
            () -> new NotFoundException("Group not found")
        );

        if (userService.getUser() != group.getOwner()) {
            throw new UserNotAuthorizedException("User not authorized to update this group");
        }

        group.setName(dto.name());
        group = groupRepository.save(group);
        return group.getGroupDetailDto();
    }

    @Override
    @Transactional
    public GroupDetailDto updateGroupOwner(GroupMemberDto dto) {
        LOG.trace("updateGroupOwner({})", dto);

        Group group = groupRepository.findById(dto.id()).orElseThrow(
            () -> new NotFoundException("Group not found")
        );

        if (userService.getUser() != group.getOwner()) {
            throw new UserNotAuthorizedException("User not authorized to update this group");
        }

        ApplicationUser newOwner = userRepository.findByUsername(dto.username()).orElseThrow(
            () -> new NotFoundException("User not found"));

        if (!group.getMembers().contains(newOwner)) {
            throw new ValidationException("Validation failed for updating Group", List.of("New Owner not yet member of this group"));
        }

        permissionRepository.removeAllByGroup(group);
        group.setOwner(newOwner);
        group = groupRepository.save(group);
        return group.getGroupDetailDto();
    }

    @Override
    @Transactional
    public GroupDetailWithMembersDto getGroup(long id) {
        LOG.trace("getGroup({})", id);
        if (!groupRepository.existsByIdAndMembersContaining(id, userService.getUser())) {
            throw new UserNotAuthorizedException("User not member of this group or group does not exist");
        }
        return groupRepository.findById(id).map(Group::getGroupDetailWithMembersDto).orElseThrow(() -> new NotFoundException("Group not found"));
    }

    @Override
    @Transactional
    public Set<GroupDetailDto> getAllGroups() {
        LOG.trace("getAllGroups");
        return groupRepository.findByMembersContaining(userService.getUser())
            .stream()
            .map(Group::getGroupDetailDto).collect(Collectors.toSet());
    }

    @Override
    public Set<GroupDto> getMyGroups() {
        LOG.trace("getMyGroups");
        return groupRepository.findByOwner(userService.getUser())
            .stream()
            .map(Group::getGroupDto).collect(Collectors.toSet());
    }

    @Override
    public Set<GroupDto> getCommonGroups(String username) {
        LOG.trace("getCommonGroups({})", username);
        Set<GroupDto> ownerGroups = groupRepository.findByOwner(userService.getUser()).stream().map(Group::getGroupDto).collect(Collectors.toSet());
        Set<GroupDto> memberGroups = groupRepository.findByMembersContaining(userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"))).stream().map(Group::getGroupDto).collect(Collectors.toSet());
        return ownerGroups.stream().filter(memberGroups::contains).collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void deleteGroup(Long id) {
        LOG.trace("deleteGroup({})", id);
        Group group = groupRepository.findById(id).orElseThrow(
            () -> new NotFoundException("Group not found")
        );

        if (!group.getOwner().equals(userService.getUser())) {
            throw new UserNotAuthorizedException("User not authorized to delete this group");
        }

        groupRepository.delete(group);
    }
}
