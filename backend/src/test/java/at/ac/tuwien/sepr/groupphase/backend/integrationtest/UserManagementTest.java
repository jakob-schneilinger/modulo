package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
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
    private AuthenticationManager authenticationManager;

    // ##########  User Create Tests  ##########

    public UserCreateDto createUserDto(String username, String displayName, String email, String password) {
        var dto = new UserCreateDto();
        dto.setUsername(username);
        dto.setDisplayName(displayName);
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    @Test
    @Transactional
    public void createUserReturns200() throws Exception {
        var dto = createUserDto("seb_01", "Sebi", "sebi@ibes.oarg", "1234567890");

        var mvcResponse = this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        assertEquals(HttpStatus.OK.value(), mvcResponse.getStatus());
    }

    @Test
    @Transactional
    public void createUserReturnsValidUserJWT() throws Exception {
        var dto = createUserDto("seb_01", "Sebi", "sebi@ibes.oarg", "1234567890");

        var mvcResponse = this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        JwtResponseDto tokenDto = objectMapper.readerFor(JwtResponseDto.class)
                .readValue(mvcResponse.getContentAsByteArray());

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
    public void updateHorseValidateError(UserCreateDto dto) throws Exception {

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
        var mvcResponse = this.mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto).getBytes()))
                .andReturn()
                .getResponse();

        JwtResponseDto tokenDto = objectMapper.readerFor(JwtResponseDto.class)
                .readValue(mvcResponse.getContentAsByteArray());

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

    // ##########  User Update Tests  ##########

}
