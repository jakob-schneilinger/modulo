package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.FriendDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.FriendService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.FriendServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FriendServiceUnitTest {

    private ApplicationUser testUser(){
        var user =  new ApplicationUser();
        user.setId(1L);
        user.setUsername("Adelheid");
        user.setEmail("ade@mail.com");
        return user;
    }
    private ApplicationUser friend(){
        var user =  new ApplicationUser();
        user.setId(2L);
        user.setUsername("Arsen");
        user.setEmail("ars@mail.com");
        return user;
    }
    private List<FriendDto> allFriends(boolean onlyFriends) {
        var me = testUser();
        ArrayList<FriendDto> friends = new ArrayList<>();

        // my requests
        int i = 0;
        for (; i < 2; i++) {
            if (onlyFriends) continue;
            String str = String.valueOf(i);
            friends.add(new FriendDto(str, str, str, me.getUsername(), false));
        }
        // requests to me
        for (; i < 4; i++) {
            if (onlyFriends) continue;
            String str = String.valueOf(i);
            friends.add(new FriendDto(str, str, str, str, false));
        }
        // accepted
        for (; i < 6; i++) {
            String str = String.valueOf(i);
            friends.add(new FriendDto(str, str, str, me.getUsername(), true));
        }

        return friends;
    }
    private UserRepository mockUserRepository(ApplicationUser requester, ApplicationUser accepter){
        // set context holder with mocked context
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // set mocked user to return from authentication
        when(authentication.getName()).thenReturn(requester.getUsername());

        // mock user repository
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername(requester.getUsername())).thenReturn(Optional.of(requester));
        when(userRepository.findByUsername(friend().getUsername())).thenReturn(Optional.of(accepter));

        return userRepository;
    }
    @Test
    void contextLoads() {
        new FriendServiceImpl(mockUserRepository(testUser(), friend()));
    }

    //  FriendService.requestFriendship()
    @Test
    void sendsRequestWithNonExistingUsersFails() {
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me, friend());

        String nobody = "nobody";

        FriendService friendService = new FriendServiceImpl(userRepository);
        assertThatThrownBy(()->friendService.requestFriendship(me.getUsername(), nobody))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void sendRequestOnlyFromMyself() {
        var friend = friend();
        UserRepository userRepository = mockUserRepository(testUser(), friend);

        String nobody = "nobody";
        FriendService friendService = new FriendServiceImpl(userRepository);

        assertThatThrownBy(()->friendService.requestFriendship(friend.getUsername(), nobody))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cannotSendDoubleRequest() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);

        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);
        assertThatThrownBy(()->friendService.requestFriendship(me.getUsername(), friend.getUsername()))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotSendRequestToMyself() {
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me, friend());

        FriendService friendService = new FriendServiceImpl(userRepository);
        assertThatThrownBy(()->friendService.requestFriendship(me.getUsername(), me.getUsername()))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void sendRequestCallsRepository() {

        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);

        FriendService friendService = new FriendServiceImpl(userRepository);
        friendService.requestFriendship(me.getUsername(), friend.getUsername());

        verify(userRepository, times(1)).createFriendRequest(me.getId(), friend.getId());
    }

    //  FriendService.acceptFriendship()
    @Test
    void cannotAcceptNonexistentRequest() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);

        FriendService friendService = new FriendServiceImpl(userRepository);

        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(false);
        assertThatThrownBy(() -> friendService.acceptFriendship(me.getUsername(), friend.getUsername()))
            .isInstanceOf(ConflictException.class);
    }
    @Test
    void cannotAcceptMyself() {
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me, me);

        FriendService friendService = new FriendServiceImpl(userRepository);

        when(userRepository.requestExists(me.getId(), me.getId())).thenReturn(true);
        assertThatThrownBy(() -> friendService.acceptFriendship(me.getUsername(), me.getUsername()))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotAcceptForOtherUser() {
        // also covers if request is sent by me

        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        assertThatThrownBy(() -> friendService.acceptFriendship(friend.getUsername(), me.getUsername()))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void acceptFriendshipCallsRepository(){
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        friendService.acceptFriendship(me.getUsername(), friend.getUsername());

        verify(userRepository, times(1)).acceptFriendRequest(me.getId(), friend.getId());
    }

    //  FriendService.deleteFriend()
    @Test
    void cannotDeleteFriendshipOfOtherUsers() {
        var me = testUser();
        var friend = friend();
        String nobody = "nobody";
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(false);

        FriendService friendService = new FriendServiceImpl(userRepository);
        assertThatThrownBy(() -> friendService.deleteFriendship(friend.getUsername(), nobody)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> friendService.deleteFriendship(nobody, friend().getUsername())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteFriendshipCallsRepository() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        friendService.deleteFriendship(me.getUsername(), friend.getUsername());

        verify(userRepository, times(1)).deleteFriend(me.getId(), friend.getId());
    }


    //  FriendService.getAllFriends()

    @Test
    void cannotGetAllFriendOfOtherUser() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        assertThatThrownBy(() -> friendService.getAllFriends(friend.getUsername(), false)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cannotGetRequestsWhenOnlyFriends() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);
        when(userRepository.getAllFriends(me.getId(), true)).thenReturn(allFriends(true));
        when(userRepository.getAllFriends(me.getId(), false)).thenReturn(allFriends(false));

        FriendService friendService = new FriendServiceImpl(userRepository);

        List<FriendDto> onlyFriends = friendService.getAllFriends(me.getUsername(), true);
        assertThat(onlyFriends.stream().filter(FriendDto::accepted)).hasSize(2);

        verify(userRepository, times(1)).getAllFriends(me.getId(), true);
    }

    @Test
    void getsAllFriends() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.getAllFriends(me.getId(), true)).thenReturn(allFriends(true));
        when(userRepository.getAllFriends(me.getId(), false)).thenReturn(allFriends(false));

        FriendService friendService = new FriendServiceImpl(userRepository);

        List<FriendDto> onlyFriends = friendService.getAllFriends(me.getUsername(), false);
        assertThat(onlyFriends).hasSize(6);

        verify(userRepository, times(1)).getAllFriends(me.getId(), false);
    }

    //  FriendService.isFriends()

    @Test
    void recognizesFriend() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.isFriend(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        boolean isFriend = friendService.isFriend(me.getUsername(), friend.getUsername());

        assertThat(isFriend).isTrue();
        verify(userRepository, times(1)).isFriend(me.getId(), friend.getId());
    }

    @Test
    void recognizesNonFriend() {
        var me = testUser();
        var nonFriend = friend();
        UserRepository userRepository = mockUserRepository(me, nonFriend);
        when(userRepository.isFriend(me.getId(), nonFriend.getId())).thenReturn(false);

        FriendService friendService = new FriendServiceImpl(userRepository);

        boolean isFriend = friendService.isFriend(me.getUsername(), nonFriend.getUsername());

        assertThat(isFriend).isFalse();
        verify(userRepository, times(1)).isFriend(me.getId(), nonFriend.getId());
    }

    //  FriendService.getFriend()

    @Test
    void cannotGetFriendOfOtherUser() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);

        FriendService friendService = new FriendServiceImpl(userRepository);

        assertThatThrownBy(() -> friendService.getFriend(friend.getUsername(), me.getUsername())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canGetFriendRequest() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.isFriend(me.getId(), friend.getId())).thenReturn(false);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);
        when(userRepository.iRequested(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        FriendDto friendDto = friendService.getFriend(me.getUsername(), friend.getUsername());

        assertThat(friendDto).isNotNull();
        assertThat(friendDto.username()).isEqualTo(friend.getUsername());
        assertThat(friendDto.accepted()).isEqualTo(false);
    }

    @Test
    void canGetActualFriend() {
        var me = testUser();
        var friend = friend();
        UserRepository userRepository = mockUserRepository(me, friend);
        when(userRepository.isFriend(me.getId(), friend.getId())).thenReturn(true);
        when(userRepository.requestExists(me.getId(), friend.getId())).thenReturn(true);
        when(userRepository.iRequested(me.getId(), friend.getId())).thenReturn(true);

        FriendService friendService = new FriendServiceImpl(userRepository);

        FriendDto friendDto = friendService.getFriend(me.getUsername(), friend.getUsername());

        assertThat(friendDto).isNotNull();
        assertThat(friendDto.username()).isEqualTo(friend.getUsername());
        assertThat(friendDto.accepted()).isEqualTo(true);

    }



}
