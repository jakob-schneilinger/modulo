
package at.ac.tuwien.sepr.groupphase.backend.integrationtest.componentEndpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Password;
import at.ac.tuwien.sepr.groupphase.backend.entity.Salt;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComponentEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    private static String userToken;
    private final String myUsername = "rin";
    private final String otherUsername = "emiya";

    private static Long parentId;
    private static String otherUserToken;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ComponentRepository componentRepository;

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


    // set up a user to be able to interact with components, as we need it for auth and rest methods
    // -> maybe do a config for test or mock some data temporarily
    @BeforeEach
    void setup()
        throws Exception {

        userRepository.deleteAll();
        componentRepository.deleteAll();

        ApplicationUser me = userRepository.save(testUser(myUsername));
        ApplicationUser other = userRepository.save(testUser(otherUsername));

        parentId = componentRepository.save(parentBoard(me.getId())).getId();
        componentRepository.flush();

        userToken = jwtTokenizer.getAuthToken(me.getUsername(), me.getDisplayName(), me.getEmail());
        otherUserToken = jwtTokenizer.getAuthToken(other.getUsername(), other.getDisplayName(), other.getEmail());
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        userRepository.flush();

        componentRepository.deleteAll();
        componentRepository.flush();
    }


    Board parentBoard(Long ownerId) throws Exception {
        // having a test datasource would of course help
        Board root = new Board();
        root.setBoardName("root");
        root.setRow(1L);
        root.setColumn(1L);
        root.setWidth(1L);
        root.setHeight(1L);
        root.setOwnerId(ownerId);

        return root;
    }


    public static UserCreateDto createUserDto(String username, String displayName, String email, String password) {
        var dto = new UserCreateDto();
        dto.setUsername(username);
        dto.setDisplayName(displayName);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    @Test
    void contextLoads() throws Exception {
    }

    @Test
    void canCreateBoardWithoutParentReturns201() throws Exception {
        BoardCreateDto boardCreateDto = new BoardCreateDto("new board", null, null, 1L,1L,2L,2L);

        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardCreateDto))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isCreated());

        BoardDetailDto responseBoard = objectMapper.
            readerFor(BoardDetailDto.class).
                <BoardDetailDto>readValue(response
                .andReturn()
                .getResponse()
                .getContentAsByteArray());

        assertThat(responseBoard).isNotNull();
        assertThat(responseBoard.name()).isEqualTo("new board");
    }


    @Test
    void createWithNonexistentParentReturns404() throws Exception {
        BoardCreateDto boardCreateDto = new BoardCreateDto("board test", null, -99L, 1L,1L,3L,3L);

        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardCreateDto))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isNotFound());

    }

    @Test
    void createWithParentCorrectly() throws Exception {

        BoardCreateDto boardChild = new BoardCreateDto("board test child", null, parentId, 1L,1L,4L,4L);
        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isCreated());

        BoardDetailDto responseBoard = objectMapper.
            readerFor(BoardDetailDto.class).
                <BoardDetailDto>readValue(response
                .andReturn()
                .getResponse()
                .getContentAsByteArray());

        assertThat(responseBoard).isNotNull();
        assertThat(responseBoard.name()).isEqualTo("board test child");
    }


    @Test
    void createBoardWithParentOfOtherUserReturns403() throws Exception {
        BoardCreateDto boardChild = new BoardCreateDto("board test child", null, parentId, 1L,1L,5L,5L);
        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", otherUserToken)
        );
        response.andExpect(status().isForbidden());
    }

    @Test
    void createOnPositionOfExistingBoardReturns409() throws Exception {
        BoardCreateDto boardCreateDto = new BoardCreateDto("new board", null, parentId, 1L,1L,6L,6L);

        mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardCreateDto))
                .header("Authorization", userToken)
        );

        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardCreateDto))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isConflict());
    }

    @Test
    void getExistingBoardAndReturn200() throws Exception {
        var response = mockMvc.perform(
            get("/api/v1/component")
                .header("Authorization", userToken)
        );
        response.andExpect(status().isOk());
    }

    @Test
    void deleteExistingBoardAndReturn200() throws Exception {
        var response = mockMvc.perform(
            delete("/api/v1/component/" + parentId)
                .header("Authorization", userToken)
        );
        response.andExpect(status().isOk());
    }

    @Test
    void deleteNonExistingBoardReturns404() throws Exception {
        mockMvc.perform(
            delete("/api/v1/component/"+ -100)
                .header("Authorization", userToken)
        ).andExpect(status().isNotFound());
    }

    @Test
    void deleteBoardOfOtherUserReturns403() throws Exception {
        mockMvc.perform(
            delete("/api/v1/component/" + parentId)
                .header("Authorization", otherUserToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    void updateExistingBoardAndReturn200() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name", null, null, 1L,1L,7L,7L);
        var response = mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isOk()); //is modified??

        BoardDetailDto responseBoard = objectMapper.
            readerFor(BoardDetailDto.class).
                <BoardDetailDto>readValue(response
                .andReturn()
                .getResponse()
                .getContentAsByteArray());

        assertThat(responseBoard).isNotNull();
        assertThat(responseBoard.id()).isEqualTo(parentId);
        assertThat(responseBoard.name()).isEqualTo("new name");
    }


    @Test
    void updateNonexistentBoardReturns404() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(-100, "new name", null, null, 1L,1L,8L,8L);
        var response = mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isNotFound());
    }

    @Test
    void updateBoardOfOtherUserReturns403() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name", null, null, 1L,1L,9L,9L);
        var response = mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", otherUserToken)
        );
        response.andExpect(status().isForbidden());
    }

    @Test
    void updateWithSelfAsParentReturns409() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name", null, parentId, 1L,1L,10L,10L);
        var response = mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isConflict());
    }

    @Test
    void updateBoardAssignParentBoardAndReturn200() throws Exception {

        BoardCreateDto boardChild = new BoardCreateDto("new board", null, null, 1L, 1L, 11L, 11L);
        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isCreated());

        Long childId = objectMapper.
            readerFor(BoardDetailDto.class).
                <BoardDetailDto>readValue(response
                .andReturn()
                .getResponse()
                .getContentAsByteArray()).id();

        BoardUpdateDto updateChild = new BoardUpdateDto(childId, "new board", null, parentId, 1L, 1L, 11L, 11L);

        mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateChild))
                .header("Authorization", userToken)
        ).andExpect(status().isOk());
    }
}