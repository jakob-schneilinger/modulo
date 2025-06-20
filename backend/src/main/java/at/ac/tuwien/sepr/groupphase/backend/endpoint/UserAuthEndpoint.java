package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
