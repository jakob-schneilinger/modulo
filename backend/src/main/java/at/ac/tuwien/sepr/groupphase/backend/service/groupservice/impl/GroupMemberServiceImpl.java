package at.ac.tuwien.sepr.groupphase.backend.service.groupservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Set;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public GroupMemberServiceImpl(GroupRepository groupRepository, UserRepository userRepository, UserService userService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    public void addGroupMember(GroupMemberDto dto) {
        LOG.trace("addGroupMember({})", dto);
        ApplicationUser member = userRepository.findByUsername(dto.username()).orElseThrow(
            () -> new NotFoundException("User not found")
        );

        Group group = groupRepository.findById(dto.id()).orElseThrow(
            () -> new NotFoundException("Group not found")
        );

        if (!group.getOwner().equals(userService.getUser())) {
            throw new UserNotAuthorizedException("User not authorized to modify this group");
        }

        Set<ApplicationUser> members = group.getMembers();
        members.add(member);
        group.setMembers(members);
    }

    @Override
    @Transactional
    public void deleteGroupMember(GroupMemberDto dto) {
        LOG.trace("deleteGroupMember({})", dto);
        removeMember(userRepository.findByUsername(dto.username()).orElseThrow(
            () -> new NotFoundException("User not found")), dto.id());
    }

    @Override
    @Transactional
    public void leaveGroup(long id) {
        LOG.trace("leaveGroup({})", id);
        removeMember(userService.getUser(), id);
    }

    private void removeMember(ApplicationUser user, long groupId) {
        LOG.trace("removeMember({}, {})", user, groupId);
        Group group = groupRepository.findById(groupId).orElseThrow(
            () -> new NotFoundException("Group not found")
        );

        ApplicationUser owner = group.getOwner();

        if (!owner.equals(userService.getUser())) {
            throw new UserNotAuthorizedException("User not authorized to modify this group");
        }

        if (user.equals(owner)) {
            throw new ValidationException("Owner is not allowed to leave its group", new ArrayList<>());
        }

        Set<ApplicationUser> members = group.getMembers();
        members.remove(user);
        group.setMembers(members);
    }
}
