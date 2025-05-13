package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import java.io.File;
import java.io.IOException;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.JwtResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;

//@CrossOrigin
@RestController
@RequestMapping(value = "/api/v1/user")
public class UserEndpoint {

    @Value("${global.location.avatars}")
    private String avatarPath;

    private UserService userService;
    private AuthService authService;

    public UserEndpoint(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostConstruct
    public void init() {
        File dir = new File(avatarPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @PermitAll
    @PostMapping("register")
    public ResponseEntity<JwtResponseDto> register(@RequestBody UserCreateDto userCreateDto) {
        return ResponseEntity.ok(new JwtResponseDto(authService.create(userCreateDto)));
    }

    @PermitAll // TODO: don't permit all?
    @GetMapping("{username}")
    public ResponseEntity<UserDto> getUser(
            @PathVariable(name = "username") String username,
            @RequestHeader(name = "Authorization") String token) {
        return ResponseEntity.ok(userService.get(username));
    }

    @PermitAll // TODO: don't permit all?
    @PatchMapping("{username}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable(name = "username") String username,
            @RequestBody UserUpdateDto userUpdateDto,
            @RequestHeader(name = "Authorization") String token) {
        userService.update(username, userUpdateDto);
        return ResponseEntity.ok().build();
    }

    @PermitAll // TODO: don't permit all?
    @DeleteMapping("{username}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable(name = "username") String username,
            @RequestHeader(name = "Authorization") String token) {
        userService.delete(username);
        return ResponseEntity.ok().build();
    }

    @PermitAll // TODO: don't permit all?
    @GetMapping("{username}/avatar")
    public ResponseEntity<FileSystemResource> getAvatar(@PathVariable(name = "username") String username) {
        File image = new File(avatarPath + username);
        if (!image.exists()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        FileSystemResource resource = new FileSystemResource(image);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + username);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @PermitAll // TODO: don't permit all?
    @PostMapping("{username}/avatar")
    public ResponseEntity<Void> setAvatar(
            @PathVariable(name = "username") String username,
            @RequestParam("file") MultipartFile file) {
        try {
            userService.saveAvatar(username, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }

    @PermitAll // TODO: don't permit all?
    @DeleteMapping("{username}/avatar")
    public ResponseEntity<Void> removeAvatar(@PathVariable(name = "username") String username) {
        userService.removeAvatar(username);
        return ResponseEntity.ok().build();

    }
}
