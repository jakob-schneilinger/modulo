package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.exception.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.GroupRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PermissionRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupService;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.impl.GroupServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GroupServiceUnitTest {

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

    private Group group(ApplicationUser owner, Set<ApplicationUser> members) {
        var group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setOwner(owner);
        group.setMembers(members);
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
        var owner = owner();
        var member = member();
        new GroupServiceImpl(
            mockGroupRepo(group(owner, Set.of(member)), owner),
            null,
            mockUserRepo(owner, member),
            null
        );
    }

    @Test
    void createGroupReturnsGroupDetail() {
        var owner = owner();
        var groupRepo = mock(GroupRepository.class);
        var userRepo = mock(UserRepository.class);
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);
        when(groupRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GroupService service = new GroupServiceImpl(groupRepo, userService, userRepo, null);
        var result = service.createGroup();

        assertThat(result).isNotNull();
        verify(groupRepo).save(any(Group.class));
    }

    @Test
    void updateGroupWithEmptyNameFails() {
        var service = new GroupServiceImpl(mock(GroupRepository.class), mock(UserService.class), mock(UserRepository.class), null);
        assertThatThrownBy(() -> service.updateGroupName(new GroupDto(1L, "")))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateGroupNameWithoutPermissionFails() {
        var owner = owner();
        var member = member();
        var group = group(owner, Set.of(owner));
        var repo = mock(GroupRepository.class);
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(member);

        when(repo.findById(1L)).thenReturn(Optional.of(group));

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        assertThatThrownBy(() -> service.updateGroupName(new GroupDto(1L, "NewName")))
            .isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void updateGroupNameWorks() {
        var owner = owner();
        var group = group(owner, Set.of(owner));
        var repo = mock(GroupRepository.class);
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        when(repo.findById(1L)).thenReturn(Optional.of(group));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        var result = service.updateGroupName(new GroupDto(1L, "Renamed Group"));

        assertThat(result.name()).isEqualTo("Renamed Group");
    }

    @Test
    void updateGroupOwnerFailsIfUserNotFound() {
        var owner = owner();
        var group = group(owner, Set.of(owner));
        var repo = mock(GroupRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(group));

        var userService = mock(UserService.class);
        when(userService.getUser()).thenReturn(owner);

        var userRepo = mock(UserRepository.class);
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        GroupService service = new GroupServiceImpl(repo, userService, userRepo, null);

        assertThatThrownBy(() -> service.updateGroupOwner(new GroupMemberDto(1L, "unknown")))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateGroupOwnerFailsIfNotMember() {
        var owner = owner();
        var member = member();
        var group = group(owner, Set.of(owner));
        var repo = mock(GroupRepository.class);
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        when(repo.findById(1L)).thenReturn(Optional.of(group));

        var userRepo = mock(UserRepository.class);
        when(userRepo.findByUsername(member.getUsername())).thenReturn(Optional.of(member));

        GroupService service = new GroupServiceImpl(repo, userService, userRepo, null);

        assertThatThrownBy(() -> service.updateGroupOwner(new GroupMemberDto(1L, member.getUsername())))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void updateGroupOwnerWorks() {
        var member = member();
        var owner = owner();
        var group = group(owner, Set.of(owner, member));
        var repo = mock(GroupRepository.class);
        var userService = mock(UserService.class);

        when(userService.getUser()).thenReturn(owner);

        when(repo.findById(1L)).thenReturn(Optional.of(group));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var userRepo = mock(UserRepository.class);
        when(userRepo.findByUsername(member.getUsername())).thenReturn(Optional.of(member));

        var permissionRepo = mock(PermissionRepository.class);

        GroupService service = new GroupServiceImpl(repo, userService, userRepo, permissionRepo);

        var result = service.updateGroupOwner(new GroupMemberDto(1L, member.getUsername()));
        assertThat(result.owner().username()).isEqualTo(member.getUsername());
    }

    @Test
    void getGroupAsNonMemberFails() {
        var member = member();

        var repo = mock(GroupRepository.class);
        when(repo.existsByIdAndMembersContaining(1L, member)).thenReturn(false);

        var userService = mock(UserService.class);
        when(userService.getUser()).thenReturn(member);

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        assertThatThrownBy(() -> service.getGroup(1L)).isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void getAllGroupsReturnsList() {
        var owner = owner();
        var group1 = group(owner, Set.of(owner));
        var group2 = group(owner, Set.of(owner));
        group2.setId(2L);
        group2.setName("Other");

        var repo = mock(GroupRepository.class);
        when(repo.findByMembersContaining(owner)).thenReturn(Set.of(group1, group2));

        var userService = mock(UserService.class);
        when(userService.getUser()).thenReturn(owner);

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        var result = service.getAllGroups();

        assertThat(result).hasSize(2);
    }

    @Test
    void getMyGroupsReturnsList() {
        var owner = owner();
        var g1 = group(owner, new HashSet<>(Set.of(owner)));
        var g2 = group(owner, new HashSet<>(Set.of(owner)));
        g2.setId(2L);
        g2.setName("Another");

        var repo = mock(GroupRepository.class);
        when(repo.findByOwner(owner)).thenReturn(Set.of(g1, g2));

        var userService = mock(UserService.class);
        when(userService.getUser()).thenReturn(owner);

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        var result = service.getMyGroups();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteGroupWithoutPermissionFails() {
        var owner = owner();
        var member = member();
        var group = group(owner, Set.of(owner));
        var repo = mock(GroupRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(group));

        var userService = mock(UserService.class);
        when(userService.getUser()).thenReturn(member);

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        assertThatThrownBy(() -> service.deleteGroup(1L)).isInstanceOf(UserNotAuthorizedException.class);
    }

    @Test
    void deleteGroupWorks() {
        var owner = owner();
        var group = group(owner, Set.of(owner));
        var repo = mock(GroupRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(group));

        var userService = mock(UserService.class);
        when(userService.getUser()).thenReturn(owner);

        GroupService service = new GroupServiceImpl(repo, userService, mock(UserRepository.class), null);
        service.deleteGroup(1L);

        verify(repo).delete(group);
    }
}
