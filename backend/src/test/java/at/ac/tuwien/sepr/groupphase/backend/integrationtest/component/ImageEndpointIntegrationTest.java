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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ComponentRepository componentRepository;

    // set up a user to be able to interact with components, as we need it for auth and rest methods
    // -> maybe do a config for test or mock some data temporarily

    /**
     * Setup.
     *
     * @throws Exception exception
     */
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

        parentBoard(mockMvc, objectMapper);
    }

    // TODO: add comment
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

    /**
     * Cleanup.
     *
     * @throws Exception exception
     */
    @AfterAll
    static void cleanup() throws Exception {
        Files.deleteIfExists(Path.of("database/testdb.mv.db"));
        Path imagePath = Path.of("res/test_images");
        if (Files.exists(imagePath)) {
            List<Path> paths = Files.walk(imagePath).sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    /**
     * Returns a new user dto.
     *
     * @param username the username
     * @param displayName the display name
     * @param email the email address
     * @param password the password
     * @return the newly created user dto
     */
    public static UserCreateDto createUserDto(String username, String displayName, String email, String password) {
        var dto = new UserCreateDto();
        dto.setUsername(username);
        dto.setDisplayName(displayName);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    /**
     * Loads context.
     *
     * @throws Exception exception
     */
    @Test
    void contextLoads() throws Exception {
    }

    /**
     * Tests if creating an image component without a file returns the correct status code.
     *
     * @throws Exception exception
     */
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

    /**
     * Tests if creating an image component with a valid file returns the correct file on retrieve.
     *
     * @throws Exception exception
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithFileAndRetrieveShouldReturnFile() throws Exception {
        // Prepare component metadata
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 20, 20);

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

    /**
     * Tests if creating an image component without a file and retrieving afterward returns the correct status code.
     *
     * @throws Exception exception
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithoutFileAndRetrieveShouldReturnNotFound() throws Exception {
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 30, 30);

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

    /**
     * Tests if creating an image component with an oversized file returns the correct status code.
     *
     * @throws Exception exception
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithOversizedFileShouldReturn400() throws Exception {
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 40, 40);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "component",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(imageDto)
        );

        // Create large dummy file (5MB, JPEG)
        byte[] largeImage = new byte[5 * 1024 * 1024];
        largeImage[0] = (byte) 0xFF; largeImage[1] = (byte) 0xD8; largeImage[2] = (byte) 0xFF;

        MockMultipartFile filePart = new MockMultipartFile(
            "image",
            "testLarge.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            largeImage
        );

        mockMvc.perform(
            multipart("/api/v1/component/image")
                .file(jsonPart)
                .file(filePart)
                .header("Authorization", userToken)
        ).andExpect(status().isBadRequest());
    }

    /**
     * Tests if creating an image component with an unsupported file type returns the correct status code.
     *
     * @throws Exception exception
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithUnsupportedFileTypeShouldReturn400() throws Exception {
        ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 50, 50);

        MockMultipartFile jsonPart = new MockMultipartFile(
            "component",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(imageDto)
        );

        byte[] fakeImage = "ThisIsNotAnImage".getBytes();

        MockMultipartFile filePart = new MockMultipartFile(
            "image",
            "fake.img",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            fakeImage
        );

        mockMvc.perform(
            multipart("/api/v1/component/image")
                .file(jsonPart)
                .file(filePart)
                .header("Authorization", userToken)
        ).andExpect(status().isBadRequest());
    }

    /**
     * Tests if creating image components with correct file types return the correct status code.
     *
     * @throws Exception exception
     */
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void createImageComponentWithValidFileTypesShouldReturn200() throws Exception {

        record TestImage(String name, byte[] data, MediaType type, int expectedStatus) {}

        ArrayList<TestImage> validImages = new ArrayList<>(List.of(
            new TestImage("valid.jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00}, MediaType.IMAGE_JPEG, 200),
            new TestImage("valid.png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}, MediaType.IMAGE_PNG, 200),
            new TestImage("valid1.gif", new byte[] {0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, MediaType.IMAGE_GIF, 200),
            new TestImage("valid2.gif", new byte[] {0x47, 0x49, 0x46, 0x38, 0x39, 0x61}, MediaType.IMAGE_GIF, 200)
        ));

        for (int i = 0; i < validImages.size(); i++) {
            ImageCreateDto imageDto = new ImageCreateDto(parentId, 1, 1, 60 + i * 10L, 60 + i * 10L);

            TestImage validImage = validImages.get(i);

            MockMultipartFile jsonPart = new MockMultipartFile(
                "component",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(imageDto)
            );

            MockMultipartFile filePart = new MockMultipartFile(
                "image",
                validImage.name,
                validImage.type.toString(),
                validImage.data
            );

            mockMvc.perform(
                multipart("/api/v1/component/image")
                    .file(jsonPart)
                    .file(filePart)
                    .header("Authorization", userToken)
            ).andExpect(status().isOk());
        }
    }
}