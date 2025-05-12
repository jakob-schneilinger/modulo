package at.ac.tuwien.sepr.groupphase.backend.basetest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;

public interface TestData {

    static UserCreateDto getDefaultUserCreateDto() {
        UserCreateDto dto = new UserCreateDto();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        return dto;
    }

    static UserLoginDto getDefaultUserLoginDto() {
        return new UserLoginDto("testuser", "password123");
    }

    static UserCreateDto withUsername(UserCreateDto original, String username) {
        UserCreateDto copy = new UserCreateDto();
        copy.setUsername(username);
        copy.setEmail(original.getEmail());
        copy.setPassword(original.getPassword());
        return copy;
    }

    static UserCreateDto withEmail(UserCreateDto original, String email) {
        UserCreateDto copy = new UserCreateDto();
        copy.setUsername(original.getUsername());
        copy.setEmail(email);
        copy.setPassword(original.getPassword());
        return copy;
    }

    static UserCreateDto withPassword(UserCreateDto original, String password) {
        UserCreateDto copy = new UserCreateDto();
        copy.setUsername(original.getUsername());
        copy.setEmail(original.getEmail());
        copy.setPassword(password);
        return copy;
    }

    static UserLoginDto withUsername(UserLoginDto original, String username) {
        return new UserLoginDto(username, original.password());
    }

    static UserLoginDto withPassword(UserLoginDto original, String password) {
        return new UserLoginDto(original.username(), password);
    }
}
