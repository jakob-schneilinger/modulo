package at.ac.tuwien.sepr.groupphase.backend.integrationtest.component;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ImageCreateDto;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImageEndpointIntegrationTest {

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

        //TODO: Hotfix
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
    void createImageComponentWithoutFileShouldReturn200() throws Exception {
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 2, 10, 10);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "component", // Feldname muss zu deinem Controller passen
            "",          // Dateiname (optional)
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(imageDto)
        );

        var result = mockMvc.perform(
            multipart("/api/v1/component/image")
                .file(jsonPart)
                .header("Authorization", userToken)
        );

        result.andExpect(status().isOk());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithFileAndRetrieveShouldReturnFile() throws Exception {
        // Prepare component metadata
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 300, 300);

        // Prepare JSON part
        MockMultipartFile jsonPart = new MockMultipartFile(
            "component",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(imageDto)
        );

        // Prepare image file
        byte[] dummyImage = Files.readAllBytes(Path.of("src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/component/img.png")); // minimal JPEG-like header
        MockMultipartFile filePart = new MockMultipartFile(
            "image",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            dummyImage
        );

        // Create image component
        var postResult = mockMvc.perform(
                multipart("/api/v1/component/image")
                    .file(jsonPart)
                    .file(filePart)
                    .header("Authorization", userToken)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse();

        // Deserialize to get ID
        ComponentDetailDto created = objectMapper.readValue(postResult.getContentAsByteArray(), ComponentDetailDto.class);
        long imageId = created.id();

        // Now fetch the image using GET
        mockMvc.perform(
            get("/api/v1/component/image/" + imageId).header("Authorization", userToken)
            ).andExpect(status().isOk())
            .andExpect(result -> {
                byte[] content = result.getResponse().getContentAsByteArray();
                assert content.length > 0;
            });
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithoutFileAndRetrieveShouldReturnNotFound() throws Exception {
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 1, 1);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "component",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(imageDto)
        );

        var postResult = mockMvc.perform(
                multipart("/api/v1/component/image")
                    .file(jsonPart)
                    .header("Authorization", userToken)
            ).andExpect(status().isOk())
            .andReturn()
            .getResponse();

        ComponentDetailDto created = objectMapper.readValue(postResult.getContentAsByteArray(), ComponentDetailDto.class);
        long imageId = created.id();

        mockMvc.perform(
            get("/api/v1/component/image/" + imageId).header("Authorization", userToken)
        ).andExpect(status().isNotFound());
    }

}