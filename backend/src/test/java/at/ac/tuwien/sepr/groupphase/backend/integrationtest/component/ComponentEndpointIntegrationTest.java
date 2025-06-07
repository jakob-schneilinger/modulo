
package at.ac.tuwien.sepr.groupphase.backend.integrationtest.component;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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


    private static String userToken;

    private static Long parentId;
    private static String otherUserToken;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ComponentRepository componentRepository;


    // set up a user to be able to interact with components, as we need it for auth and rest methods
    // -> maybe do a config for test or mock some data temporarily
    @BeforeAll
    void setup()
        throws Exception {

        userRepository.deleteAll();
        componentRepository.deleteAll();

        var dto = createUserDto("seb_01", "Sebi", "sebi@ibes.oarg", "1234567890");

        var mvcResponse = mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
            .andReturn()
            .getResponse();

        JwtResponseDto tokenDto = objectMapper.readerFor(JwtResponseDto.class)
            .<JwtResponseDto>readValue(mvcResponse.getContentAsByteArray());

        userToken = tokenDto.token();

        var otherDto = createUserDto("othername", "other", "other@other.other", "1234567890");

        var otherMvcResponse = mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherDto).getBytes()))
            .andReturn()
            .getResponse();

        JwtResponseDto otherTokenDto = objectMapper.readerFor(JwtResponseDto.class)
            .<JwtResponseDto>readValue(otherMvcResponse.getContentAsByteArray());

        otherUserToken = otherTokenDto.token();

        parentBoard(mockMvc, objectMapper);
    }

    static void parentBoard(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        // having a test datasource would of course help
        BoardCreateDto boardParent = new BoardCreateDto("board test parent", null, 1L,1L,1L,1L);

        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardParent))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isCreated());

        parentId = objectMapper.
            readerFor(BoardDetailDto.class).
                <BoardDetailDto>readValue(response
                .andReturn()
                .getResponse()
                .getContentAsByteArray()).id();
    }

    @AfterAll
    static void cleanup() throws Exception {
        Files.deleteIfExists(Path.of("database/testdb.mv.db"));
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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void canCreateBoardWithoutParentReturns201() throws Exception {
        BoardCreateDto boardCreateDto = new BoardCreateDto("new board", null, 1L,1L,2L,2L);

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
        BoardCreateDto boardCreateDto = new BoardCreateDto("board test", -99L, 1L,1L,3L,3L);

        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardCreateDto))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isNotFound());

    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createWithParentCorrectly() throws Exception {

        BoardCreateDto boardChild = new BoardCreateDto("board test child", parentId, 1L,1L,4L,4L);
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
        BoardCreateDto boardChild = new BoardCreateDto("board test child", parentId, 1L,1L,5L,5L);
        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", otherUserToken)
        );
        response.andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createOnPositionOfExistingBoardReturns409() throws Exception {
        BoardCreateDto boardCreateDto = new BoardCreateDto("new board", parentId, 1L,1L,6L,6L);

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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void deleteExistingBoardAndReturn200() throws Exception {
        var response = mockMvc.perform(
            delete("/api/v1/component/" + parentId)
                .header("Authorization", userToken)
        );
        response.andExpect(status().isOk());
        parentBoard(mockMvc, objectMapper);
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
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateExistingBoardAndReturn200() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name",null, 1L,1L,7L,7L);
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
        BoardUpdateDto boardChild = new BoardUpdateDto(-100, "new name",null, 1L,1L,8L,8L);
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
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name",null, 1L,1L,9L,9L);
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
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name", parentId, 1L,1L,10L,10L);
        var response = mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isConflict());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateBoardAssignParentBoardAndReturn200() throws Exception {

        BoardCreateDto boardChild = new BoardCreateDto( "new board",null, 1L,1L,11L,11L);
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

        BoardUpdateDto updateChild = new BoardUpdateDto(childId, "new board", parentId, 1L, 1L, 11L, 11L);
        mockMvc.perform(
            patch("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateChild))
                .header("Authorization", userToken)
        ).andExpect(status().isOk());
    }
}
