package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotAuthorizedException;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupMemberService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.impl.GroupMemberServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GroupMemberServiceUnitTest {

    private ApplicationUser owner() {
        var user = new ApplicationUser();
        user.setId(1L);
        user.setUsername("owner");
        return user;
    }

    private ApplicationUser member() {
        var user = new ApplicationUser();
        user.setId(2L);
        user.setUsername("member");
        return user;
    }

    private Group groupOwnedByOwner() {
        var group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setOwner(owner());
        group.setMembers(Set.of());
        return group;
    }

    private UserRepository mockUserRepo(ApplicationUser owner, ApplicationUser member) {
        UserRepository userRepo = mock(UserRepository.class);
        when(userRepo.findByUsername(owner.getUsername())).thenReturn(Optional.of(owner));
        when(userRepo.findByUsername(member.getUsername())).thenReturn(Optional.of(member));
        return userRepo;
    }

    private GroupRepository mockGroupRepo(Group group, ApplicationUser owner) {
        GroupRepository groupRepo = mock(GroupRepository.class);
        when(groupRepo.findById(group.getId())).thenReturn(Optional.of(group));
        return groupRepo;
    }


    @Test
    void contextLoads() {
        new GroupMemberServiceImpl(
            mockGroupRepo(groupOwnedByOwner(), owner()),
            mockUserRepo(owner(), member()),
            null
        );
    }

    @Test
    void addMemberNotFoundUserFails() {
        var group = groupOwnedByOwner();
        var owner = group.getOwner();
        var userRepo = mockUserRepo(owner, member());
        var groupRepo = mockGroupRepo(group, owner());

        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, null);
        assertThatThrownBy(() -> service.addGroupMember(new GroupMemberDto(group.getId(), "unknown")))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addMemberToNonExistentGroupFails() {
        var userRepo = mockUserRepo(owner(), member());
        GroupRepository groupRepo = mock(GroupRepository.class);
        when(groupRepo.findById(42L)).thenReturn(Optional.empty());

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, null);

        assertThatThrownBy(() -> service.addGroupMember(new GroupMemberDto(42L, member().getUsername())))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addGroupMemberWithoutPermissionFails() {
        var group = groupOwnedByOwner();
        var userRepo = mockUserRepo(owner(), member());
        var groupRepo = mockGroupRepo(group, owner());
        var userService = mock(UserService.class);

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, userService);

        assertThatThrownBy(() -> service.addGroupMember(new GroupMemberDto(group.getId(), member().getUsername())))
            .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void addExistingMemberDoesNothing() {
        var group = groupOwnedByOwner();
        var owner = group.getOwner();
        var member = member();

        group.setMembers(new HashSet<>(Set.of(member)));

        var userRepo = mockUserRepo(owner, member);
        var groupRepo = mockGroupRepo(group, owner());
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, userService);
        service.addGroupMember(new GroupMemberDto(group.getId(), member.getUsername()));

        assertThat(group.getMembers()).contains(member);
    }

    @Test
    void addValidMemberSucceeds() {
        var group = groupOwnedByOwner();
        var owner = group.getOwner();
        var member = member();

        group.setMembers(new HashSet<>());

        var userRepo = mockUserRepo(owner, member);
        var groupRepo = mockGroupRepo(group, owner());
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, userService);
        service.addGroupMember(new GroupMemberDto(group.getId(), member.getUsername()));

        assertThat(group.getMembers()).contains(member);
    }

    @Test
    void removeMemberNotFoundFails() {
        var group = groupOwnedByOwner();
        var userRepo = mockUserRepo(owner(), member());
        var groupRepo = mockGroupRepo(group, owner());

        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, null);

        assertThatThrownBy(() -> service.deleteGroupMember(new GroupMemberDto(group.getId(), "unknown")))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeFromNonexistentGroupFails() {
        var userRepo = mockUserRepo(owner(), member());
        GroupRepository groupRepo = mock(GroupRepository.class);
        when(groupRepo.findById(99L)).thenReturn(Optional.empty());

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, null);

        assertThatThrownBy(() -> service.deleteGroupMember(new GroupMemberDto(99L, member().getUsername())))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void removeMemberWithoutPermissionFails() {
        var group = groupOwnedByOwner();
        var userRepo = mockUserRepo(owner(), member());
        var groupRepo = mockGroupRepo(group, owner());
        var userService = mock(UserService.class);

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, userService);

        assertThatThrownBy(() -> service.deleteGroupMember(new GroupMemberDto(group.getId(), member().getUsername())))
            .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void removeMemberSucceeds() {
        var group = groupOwnedByOwner();
        var owner = group.getOwner();
        var member = member();

        group.setMembers(new HashSet<>(Set.of(member)));

        var userRepo = mockUserRepo(owner, member);
        var groupRepo = mockGroupRepo(group, owner());
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, userService);

        service.deleteGroupMember(new GroupMemberDto(group.getId(), member.getUsername()));

        assertThat(group.getMembers()).doesNotContain(member);
    }

    @Test
    void removeNonMemberFailsSilently() {
        var group = groupOwnedByOwner();
        var owner = group.getOwner();
        var member = member();

        group.setMembers(new HashSet<>());      // not a member

        var userRepo = mockUserRepo(owner, member);
        var groupRepo = mockGroupRepo(group, owner());
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        GroupMemberService service = new GroupMemberServiceImpl(groupRepo, userRepo, userService);

        // Should not throw, just not remove
        service.deleteGroupMember(new GroupMemberDto(group.getId(), member.getUsername()));

        assertThat(group.getMembers()).doesNotContain(member);
    }
}
