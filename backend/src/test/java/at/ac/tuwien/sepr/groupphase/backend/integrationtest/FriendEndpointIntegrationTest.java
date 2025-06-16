package at.ac.tuwien.sepr.groupphase.backend.integrationtest;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.FriendDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Password;
import at.ac.tuwien.sepr.groupphase.backend.entity.Salt;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FriendEndpointIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String myUsername = "rin";
    private final String baseUrl = "/api/v1/user/";

    private ApplicationUser testUser(String username){
        var user =  new ApplicationUser();

        user.setUsername(username);
        user.setDisplayName(username);
        user.setEmail(username + "@mail.com");

        Password password = new Password();
        password.setUser(user);
        password.setHash("hash");
        user.setPassword(password);

        Salt salt = new Salt();
        salt.setUser(user);
        salt.setSalt("salt");
        user.setSalt(salt);

        return user;
    }

    private String getToken(ApplicationUser user) {
        return jwtTokenizer.getAuthToken(user.getUsername(), user.getDisplayName(), user.getEmail());
    }

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        userRepository.saveAndFlush(testUser(myUsername));
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {

    }

    @Test
    void canSendFriendRequest() throws Exception {
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));

        // send request http request
        var response = mockMvc.perform(
            post(baseUrl + myUsername + "/friends")
                .param("friendName", friendName)
                .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk());

        // check if friends
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        assertTrue(userRepository.requestExists(me.getId(), friend.getId()));
        assertFalse(userRepository.isFriend(me.getId(), friend.getId()));
    }

    @Test
    void cannotSendFriendRequestForOtherUser() throws Exception {
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));

        // send request http request
        mockMvc.perform(
                post(baseUrl + friendName + "/friends")
                    .param("friendName", myUsername)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isForbidden());
    }


    @Test
    void cannotSendRequestToNonExistentUser() throws Exception {
        // setup - do not save user
        String friendName = "emiya";

        // send request http request
        mockMvc.perform(
                post(baseUrl + myUsername + "/friends")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isNotFound());
    }

    @Test
    void canAcceptRequest() throws Exception {
        // setup - create request
        String requesterName = "emiya";
        var requester = userRepository.saveAndFlush(testUser(requesterName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(requester.getId(), me.getId());

        // send request
        mockMvc.perform(
                put(baseUrl + myUsername + "/friends")
                    .param("friendName", requesterName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk());

        // check if accepted
        assertTrue(userRepository.requestExists(me.getId(), requester.getId()));
        assertTrue(userRepository.isFriend(me.getId(), requester.getId()));
    }

    @Test
    void cannotAcceptRequestForOtherUser() throws Exception{
        // setup
        String requesterName = "emiya";
        var requester = userRepository.saveAndFlush(testUser(requesterName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(me.getId(), requester.getId());

        // send request http request
        mockMvc.perform(
                put(baseUrl + requesterName + "/friends")
                    .param("friendName", myUsername)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isForbidden());
    }

    @Test
    void cannotAcceptNonexistentRequest() throws Exception{
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();

        // send request http request
        mockMvc.perform(
                put(baseUrl + myUsername + "/friends")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isConflict());
    }

    @Test
    void canDeleteMyRequest() throws Exception {
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(me.getId(), friend.getId());

        // send request http request
        var response = mockMvc.perform(
                delete(baseUrl + myUsername + "/friends")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk());
    }

    @Test
    void canDeleteRequestOfOtherUserToMe() throws Exception {
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(friend.getId(), me.getId());

        // send request http request
        var response = mockMvc.perform(
                delete(baseUrl + myUsername + "/friends")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk());
    }

    @Test
    void cannotDeleteRequestOfOtherUserToOtherUser() throws Exception {
        // setup
        String other_1_name = "emiya";
        var other_1 = userRepository.saveAndFlush(testUser(other_1_name));
        String other_2_name = "arturia";
        var other_2 = userRepository.saveAndFlush(testUser(other_2_name));

        userRepository.createFriendRequest(other_1.getId(), other_2.getId());

        // send request http request
        var response = mockMvc.perform(
                delete(baseUrl + other_1_name + "/friends")
                    .param("friendName", other_2_name)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isForbidden());
    }

    @Test
    void canDeleteFriend() throws Exception {
        // setup - create request - accept request
        String requesterName = "emiya";
        var requester = userRepository.saveAndFlush(testUser(requesterName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(requester.getId(), me.getId());
        userRepository.acceptFriendRequest(me.getId(), requester.getId());

        // send request
        mockMvc.perform(
                delete(baseUrl + myUsername + "/friends")
                    .param("friendName", requesterName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk());
    }

    @Test
    void canGetAllFriendsAndRequests() throws Exception {
        String[] friend_names = {"emiya", "archer", "saber"};
        List<ApplicationUser> friends = Arrays.stream(friend_names)
            .map(name -> userRepository.save(testUser(name)))
            .toList();
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(me.getId(), friends.get(0).getId());
        userRepository.acceptFriendRequest(friends.get(0).getId(), me.getId());
        userRepository.createFriendRequest(me.getId(), friends.get(1).getId());
        userRepository.createFriendRequest(friends.get(2).getId(), me.getId());

        var response = mockMvc.perform(
            get(baseUrl + myUsername + "/friends")
                .header("Authorization", getToken(testUser(myUsername)))
                .param("onlyFriends", String.valueOf(false)))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        List<FriendDto> friendDtos = objectMapper.readerFor(FriendDto.class).<FriendDto>readValues(response.getContentAsByteArray()).readAll();

        assertThat(friendDtos).isNotNull().hasSize(friends.size());

        Arrays.stream(friend_names)
            .forEach(name -> assertThat(friendDtos.stream().filter(friend -> friend.username().equals(name)).toList())
                .isNotNull()
                .hasSize(1));

    }

    @Test
    void canGetOnlyFriends() throws Exception {
        String[] friend_names = {"emiya", "archer", "saber"};
        List<ApplicationUser> friends = Arrays.stream(friend_names)
            .map(name -> userRepository.save(testUser(name)))
            .toList();
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(me.getId(), friends.get(0).getId());
        userRepository.acceptFriendRequest(friends.get(0).getId(), me.getId());
        userRepository.createFriendRequest(me.getId(), friends.get(1).getId());
        userRepository.createFriendRequest(friends.get(2).getId(), me.getId());

        var response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends")
                    .header("Authorization", getToken(testUser(myUsername)))
                    .param("onlyfriends", "true"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        List<FriendDto> friendDtos = objectMapper.readerFor(FriendDto.class).<FriendDto>readValues(response.getContentAsByteArray()).readAll();

        assertThat(friendDtos).isNotNull().hasSize(1);

        assertThat(friendDtos.stream().filter(friend -> friend.username().equals(friend_names[0])).toList())
                .isNotNull()
                .hasSize(1);

    }

    @Test
    void canGetAFriend() throws Exception {
        // setup - create request - accept request
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(friend.getId(), me.getId());
        userRepository.acceptFriendRequest(me.getId(), friend.getId());

        // send request
        var response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends/friend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        FriendDto responseDto = objectMapper.readerFor(FriendDto.class).<FriendDto>readValue(response.getContentAsByteArray());

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.username()).isEqualTo(friendName);
    }

    @Test
    void canGetRequestWhenGettingAFriend() throws Exception {
        // setup - create request
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();
        userRepository.createFriendRequest(friend.getId(), me.getId());

        // send request
        var response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends/friend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        FriendDto responseDto = objectMapper.readerFor(FriendDto.class).<FriendDto>readValue(response.getContentAsByteArray());

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.username()).isEqualTo(friendName);
    }

    @Test
    void cannotGetANonFriend() throws Exception {
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();

        // send request
        mockMvc.perform(
                get(baseUrl + myUsername + "/friends/friend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isNotFound());
    }

    @Test
    void canDetermineAWhetherUserIsFriend() throws Exception {
        // setup
        String friendName = "emiya";
        var friend = userRepository.saveAndFlush(testUser(friendName));
        var me = userRepository.findByUsername(myUsername).orElseThrow();

        // send request
        var response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends/isfriend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk()).andReturn().getResponse();

        // check for no connection between users
        Boolean isFriend = objectMapper.readerFor(Boolean.class).readValue(response.getContentAsByteArray());
        assertFalse(isFriend);

        // check for only friend request between users
        userRepository.createFriendRequest(friend.getId(), me.getId());
        // send request
        response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends/isfriend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk()).andReturn().getResponse();

        isFriend = objectMapper.readerFor(Boolean.class).readValue(response.getContentAsByteArray());
        assertFalse(isFriend);

        // check for friends
        userRepository.acceptFriendRequest(me.getId(), friend.getId());
        // send request
        response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends/isfriend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isOk()).andReturn().getResponse();

        isFriend = objectMapper.readerFor(Boolean.class).readValue(response.getContentAsByteArray());
        assertTrue(isFriend);

    }

    @Test
    void cannotGetFriendStatusOfNonexistentUser() throws Exception{
        // setup
        String friendName = "emiya";
        // send request
        var response = mockMvc.perform(
                get(baseUrl + myUsername + "/friends/isfriend")
                    .param("friendName", friendName)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isNotFound());
    }

    @Test
    void cannotGetFriendStatusForOtherUser() throws Exception{
        // setup
        String other_1_name = "emiya";
        userRepository.saveAndFlush(testUser(other_1_name));

        String other_2_name = "saber";
        userRepository.saveAndFlush(testUser(other_2_name));

        // send request
        var response = mockMvc.perform(
                get(baseUrl + other_1_name + "/friends/isfriend")
                    .param("friendName", other_2_name)
                    .header("Authorization", getToken(testUser(myUsername))))
            .andExpect(status().isForbidden());
    }





}
