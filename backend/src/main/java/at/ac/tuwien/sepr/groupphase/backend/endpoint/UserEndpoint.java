package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;

//@CrossOrigin
@RestController
@RequestMapping(value = "/api/v1/user")
public class UserEndpoint {

    private UserService userService;
    private AuthService authService;

    public UserEndpoint(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PermitAll
    @PostMapping("register")
    public ResponseEntity<JwtResponseDto> register(@RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(new JwtResponseDto(authService.create(userCreateDto)));
    }

    @PermitAll // TODO: don't permit all?
    @GetMapping("{username}")
    public ResponseEntity<String> getUser(@PathVariable(name = "username") String username) {

        return ResponseEntity.ok(username);
    }

    @PermitAll // TODO: don't permit all?
    @DeleteMapping("{username}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable(name = "username") String username,
            @RequestHeader(name = "Authorization") String token) {
        userService.delete(username, token);
        return ResponseEntity.ok().build();
    }
}
