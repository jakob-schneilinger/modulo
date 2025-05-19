
package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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



    // set up a user to be able to interact with components, as we need it for auth and rest methods
    // -> maybe do a config for test or mock some data temporarily
    @BeforeAll
    void setup()
        throws Exception {
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
        BoardCreateDto boardParent = new BoardCreateDto("board test parent", null, 1,1,1,1);

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
    void cleanup() throws Exception {
        mockMvc.perform(delete("/api/v1/user/seb_01")
                .header("Authorization", userToken));
        mockMvc.perform(delete("/api/v1/user/othername")
            .header("Authorization", otherUserToken));
        mockMvc.perform(
            delete("/api/v1/component/" + parentId)
                .header("Authorization", userToken));

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
        BoardCreateDto boardCreateDto = new BoardCreateDto("new board", null, 1,1,2,2);

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
        BoardCreateDto boardCreateDto = new BoardCreateDto("board test", -99L, 1,1,1,1);

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

        BoardCreateDto boardChild = new BoardCreateDto("board test child", parentId, 1,1,1,1);
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
        BoardCreateDto boardChild = new BoardCreateDto("board test child", parentId, 1,1,1,1);
        var response = mockMvc.perform(
            post("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", otherUserToken)
        );
        response.andExpect(status().isForbidden());
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
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name",null, 1,1,1,1);
        var response = mockMvc.perform(
            put("/api/v1/component/board")
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
        BoardUpdateDto boardChild = new BoardUpdateDto(-100, "new name",null, 1,1,1,1);
        var response = mockMvc.perform(
            put("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isNotFound());
    }

    @Test
    void updateBoardOfOtherUserReturns403() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name",null, 1,1,1,1);
        var response = mockMvc.perform(
            put("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", otherUserToken)
        );
        response.andExpect(status().isForbidden());
    }

    @Test
    void updateWithSelfAsParentReturns409() throws Exception {
        BoardUpdateDto boardChild = new BoardUpdateDto(parentId, "new name", parentId, 1,1,1,1);
        var response = mockMvc.perform(
            put("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(boardChild))
                .header("Authorization", userToken)
        );
        response.andExpect(status().isConflict());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateBoardAssignParentBoardAndReturn200() throws Exception {

        BoardCreateDto boardChild = new BoardCreateDto( "new board",null, 1,1,3,3);
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

        BoardUpdateDto updateChild = new BoardUpdateDto(childId, "new board", parentId, 1, 1, 3, 3);
        mockMvc.perform(
            put("/api/v1/component/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateChild))
                .header("Authorization", userToken)
        ).andExpect(status().isOk());
    }




}


