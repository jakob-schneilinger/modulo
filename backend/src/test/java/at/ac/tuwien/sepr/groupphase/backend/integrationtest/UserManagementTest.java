package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;

import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserManagementTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private ObjectMapper objectMapper;
    private Decoder decoder = Base64.getUrlDecoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    //TODO: Hotfix
    @BeforeAll
    void setup() {
        userRepository.deleteAll();
    }

    // ##########  User Create Tests  ##########

    public UserCreateDto createUserDto(String username, String displayName, String email, String password) {
        var dto = new UserCreateDto();
        dto.setUsername(username);
        dto.setDisplayName(displayName);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private MockHttpServletResponse createUser(UserCreateDto dto) throws Exception, JsonProcessingException {
        var mvcResponse = this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();
        return mvcResponse;
    }

    private JwtResponseDto getTokenDtoFromResponse(MockHttpServletResponse mvcResponse) throws IOException {
        JwtResponseDto tokenDto = objectMapper.readerFor(JwtResponseDto.class)
                .readValue(mvcResponse.getContentAsByteArray());
        return tokenDto;
    }

    @Test
    @Transactional
    public void createUserReturns200() throws Exception {
        var dto = createUserDto("seb_01", "Sebi", "sebi@ibes.oarg", "1234567890");

        var mvcResponse = createUser(dto);

        assertEquals(HttpStatus.OK.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void createUserReturnsValidUserJWT() throws Exception {
        var dto = createUserDto("seb_01", "Sebi", "sebi@ibes.oarg", "1234567890");
        JwtResponseDto tokenDto = getTokenDtoFromResponse(createUser(dto));

        String token = tokenDto.token();

        assertEquals(dto.getUsername(), jwtUtils.getUsernameFromJwtToken(token));
        assertEquals(dto.getEmail(), jwtUtils.getEmailFromJwtToken(token));
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    private static Stream<UserCreateDto> validationExceptionUserCreateDto() {
        return Stream.of(
                new UserCreateDto(null, null, null, null),
                new UserCreateDto("", "", "", ""),
                new UserCreateDto("admin!", null, "admin@google.com", "admin12345"),
                new UserCreateDto("admin", null, "admin@mail", "12345678"),
                new UserCreateDto("admin", null, "ad@m.in", "123"));
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("validationExceptionUserCreateDto")
    public void createUserWithValidationErrors(UserCreateDto dto) throws Exception {

        var mvcResponse = this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void createUserTwiceConflicts() throws Exception {
        var dto = new UserCreateDto("admin", null, "admin@admin.min", "1234567890");

        this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()));

        var mvcResponse = this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        String msg = mvcResponse.getContentAsString().toLowerCase();

        assertEquals(HttpStatus.CONFLICT.value(), mvcResponse.getStatus());
        assertTrue(msg.contains("username") && msg.contains("email") && msg.contains("taken"));
    }

    // ##########  User Delete Tests  ##########

    @Test
    @Transactional
    public void deleteOtherUserThanSelfReturnsForbidden() throws Exception {
        String token = jwtTokenizer.getAuthToken("max", "Maximilian", "max@max.at");

        var mvcResponse = this.mockMvc.perform(delete("/api/v1/user/sebastian")
                .header("Authorization", token))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void deleteOwnUserReturnsOk() throws Exception {
        // create user
        var dto = createUserDto("seb_01", "Sebi", "sebi@ibes.oarg", "1234567890");
        var mvcResponse = createUser(dto);

        JwtResponseDto tokenDto = getTokenDtoFromResponse(mvcResponse);

        // delete user
        mvcResponse = this.mockMvc.perform(delete("/api/v1/user/seb_01")
                .header("Authorization", tokenDto.token()))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.OK.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void deleteWithoutTokenReturnsUnauthorized() throws Exception {
        var mvcResponse = this.mockMvc.perform(delete("/api/v1/user/seb_01"))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), mvcResponse.getStatus());
    }

    // ##########  User Get Tests  ##########

    @Test
    @Transactional
    public void getUser() throws Exception {
        var userCreateDto = TestData.getDefaultUserCreateDto();
        JwtResponseDto tokenDto = getTokenDtoFromResponse(createUser(userCreateDto));

        var mvcResponse = this.mockMvc.perform(get("/api/v1/user/testuser")
                .header("Authorization", tokenDto.token()))
                .andReturn()
                .getResponse();

        UserDto user = objectMapper.readerFor(UserDto.class)
                .readValue(mvcResponse.getContentAsByteArray());

        assertEquals(userCreateDto.getUsername(), user.username());
        assertEquals(userCreateDto.getEmail(), user.email());
    }

    @Test
    @Transactional
    public void getUserUnauthorized() throws Exception {
        var userCreateDto = TestData.getDefaultUserCreateDto();
        createUser(userCreateDto);

        var mvcResponse = this.mockMvc.perform(get("/api/v1/user/testuser"))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void getNotExistingUser() throws Exception {
        var userCreateDto = TestData.getDefaultUserCreateDto();
        JwtResponseDto tokenDto = getTokenDtoFromResponse(createUser(userCreateDto));

        var mvcResponse = this.mockMvc.perform(get("/api/v1/user/maxi")
                .header("Authorization", tokenDto.token()))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void getOtherThanSelfUserOmitsEmail() throws Exception {
        var userCreateDto = TestData.getDefaultUserCreateDto();
        createUser(new UserCreateDto("maxi", "Max", "max@gmail.com", "1234567890"));
        JwtResponseDto tokenDto = getTokenDtoFromResponse(createUser(userCreateDto));

        var mvcResponse = this.mockMvc.perform(get("/api/v1/user/maxi")
                .header("Authorization", tokenDto.token()))
                .andReturn()
                .getResponse();

        UserDto user = objectMapper.readerFor(UserDto.class)
                .readValue(mvcResponse.getContentAsByteArray());

        assertEquals(user.username(), "maxi");
        assertEquals(user.email(), null);
    }

    // ##########  User Update Tests  ##########

    private static Stream<UserUpdateDto> validationExceptionUserUpdateDto() {
        return Stream.of(
                new UserUpdateDto("test", "TEST", "admin12345"),
                new UserUpdateDto(null, "admin@mail", "123"),
                new UserUpdateDto("", null, "123"));
    }

    @Transactional
    @ParameterizedTest
    @MethodSource("validationExceptionUserUpdateDto")
    public void updateUserWithValidationErrors(UserUpdateDto dto) throws Exception {
        var userCreateDto = TestData.getDefaultUserCreateDto();
        JwtResponseDto tokenDto = getTokenDtoFromResponse(createUser(userCreateDto));

        var mvcResponse = this.mockMvc
                .perform(patch("/api/v1/user/" + userCreateDto.getUsername())
                        .header("Authorization", tokenDto.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void updateUserData() throws Exception {
        var userCreateDto = TestData.getDefaultUserCreateDto();
        JwtResponseDto tokenDto = getTokenDtoFromResponse(createUser(userCreateDto));

        UserUpdateDto dto = new UserUpdateDto("test@test.test", "", "abcd1234");

        var mvcResponse = this.mockMvc
                .perform(patch("/api/v1/user/" + userCreateDto.getUsername())
                        .header("Authorization", tokenDto.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.OK.value(), mvcResponse.getStatus());
    }

    // TODO: Tests: change pwd and login

}
