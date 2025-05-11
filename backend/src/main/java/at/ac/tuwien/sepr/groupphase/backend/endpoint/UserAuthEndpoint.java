package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(value = "/api/v1/")
public class UserAuthEndpoint {
    private final AuthService authService;

    public UserAuthEndpoint(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/authentication")
    public ResponseEntity<JwtResponseDto> login(@RequestBody UserLoginDto userLoginDto) {
        return ResponseEntity.ok(new JwtResponseDto(authService.login(userLoginDto)));
    }

    @PostMapping("/user/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserCreateDto userCreateDto) {
        try {
            authService.create(userCreateDto);
            return ResponseEntity.ok("User registered successfully");
        } catch (ValidationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
