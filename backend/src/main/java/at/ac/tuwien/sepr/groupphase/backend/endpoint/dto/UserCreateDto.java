package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class UserCreateDto {

    @NotNull(message = "Name must not be null")
    private String username;

    private String displayName;

    @Email
    @NotNull(message = "Email must not be null")
    private String email;

    @NotNull(message = "Password must not be null")
    private String password;

    public UserCreateDto() {
    }

    public UserCreateDto(String username, String displayName, String email, String password) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserCreateDto userCreateDto)) {
            return false;
        }
        return Objects.equals(username, userCreateDto.username)
                && Objects.equals(email, userCreateDto.email)
                && Objects.equals(password, userCreateDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, email, password);
    }

    @Override
    public String toString() {
        return "UserLoginDto{"
                + "name='" + username + "\', "
                + "email='" + email + "\', "
                + "password='" + password + "\'"
                + '}';
    }

    public UserLoginDto asLoginDto() {
        return new UserLoginDto(username, password);
    }
}
