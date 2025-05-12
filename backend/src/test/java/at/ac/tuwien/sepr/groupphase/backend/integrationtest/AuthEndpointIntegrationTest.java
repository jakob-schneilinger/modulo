package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class AuthEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    public void register_withValidData_createsUserAndReturnsOk() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestData.getDefaultUserCreateDto())))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                JwtResponseDto.class);

        assertNotNull(response.token());
        assertFalse(response.token().isEmpty());
        assertTrue(userRepository.findByUsername("testuser").isPresent());
    }

    @Test
    public void register_withNullUsername_returnsBadRequest() throws Exception {
        UserCreateDto invalidDto = TestData.withUsername(TestData.getDefaultUserCreateDto(), null);

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void register_withInvalidEmail_returnsBadRequest() throws Exception {
        UserCreateDto invalidDto = TestData.withEmail(TestData.getDefaultUserCreateDto(), "invalid-email");

        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_withValidCredentials_returnsJwtToken() throws Exception {
        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestData.getDefaultUserCreateDto())));

        MvcResult result = mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestData.getDefaultUserLoginDto())))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                JwtResponseDto.class);
        assertNotNull(response.token());
        assertFalse(response.token().isEmpty());
    }

    @Test
    public void login_withInvalidUsername_returnsUnauthorized() throws Exception {
        UserLoginDto invalidCredentials = TestData.withUsername(TestData.getDefaultUserLoginDto(),
                "nonexistent");

        mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCredentials)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_withInvalidPassword_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TestData.getDefaultUserCreateDto())));

        UserLoginDto invalidCredentials = TestData.withPassword(TestData.getDefaultUserLoginDto(),
                "wrongpassword");

        mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCredentials)))
                .andExpect(status().isUnauthorized());
    }
}