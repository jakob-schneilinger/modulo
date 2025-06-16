package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Password;
import at.ac.tuwien.sepr.groupphase.backend.entity.Salt;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.UserServiceImpl;
import at.ac.tuwien.sepr.groupphase.backend.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceUnitTest {

    private ApplicationUser testUser(){
        var user =  new ApplicationUser();
        user.setId(1L);
        user.setUsername("Adelheid");
        user.setEmail("ade@mail.com");

        Password password = new Password();
        password.setUser(user);
        password.setHash("hash");
        user.setPassword(password);

        Salt salt = new Salt();
        salt.setUser(user);
        salt.setSalt("salt");
        user.setSalt(salt);

        return user;
    }

    private UserUpdateDto updateDto() {
        return new UserUpdateDto("newmail@mail.com", "formaldehyd", "12345678");
    }
    private ApplicationUser otherUser(){
        var user =  new ApplicationUser();
        user.setId(2L);
        user.setUsername("Arsen");
        user.setEmail("ars@mail.com");

        Password password = new Password();
        password.setUser(user);
        password.setHash("hash");
        user.setPassword(password);

        Salt salt = new Salt();
        salt.setUser(user);
        salt.setSalt("salt");
        user.setSalt(salt);
        return user;
    }

    private UserRepository mockUserRepository(ApplicationUser me){
        // set context holder with mocked context
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // set mocked user to return from authentication
        when(authentication.getName()).thenReturn(me.getUsername());

        // mock user repository
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername(me.getUsername())).thenReturn(Optional.of(me));

        return userRepository;
    }

    @Test
    void contextLoads() {
        new UserServiceImpl(mockUserRepository(testUser()), null, null, null);
    }


    // UserService.update()
    @Test
    void updateCallsRepository() {
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("hash-hush");
        UserValidator userValidator = mock(UserValidator.class);
        UserService userService = new UserServiceImpl(userRepository, null, userValidator, passwordEncoder);

        userService.update(me.getUsername(), updateDto());

        var update = updateDto();
        me.setEmail(update.email());
        me.setDisplayName(update.displayName());

        verify(userRepository).save(me);
        verify(userValidator).validateUserUpdate(updateDto());
        verify(passwordEncoder).encode(any());
    }

    @Test
    void cannotUpdateOtherUser() {
        var me = testUser();
        var other = otherUser();
        UserRepository userRepository = mockUserRepository(me);
        when(userRepository.findByUsername(other.getUsername())).thenReturn(Optional.of(other));
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("hash-hush");
        UserValidator userValidator = mock(UserValidator.class);
        UserService userService = new UserServiceImpl(userRepository, null, userValidator, passwordEncoder);

        assertThatThrownBy(() -> userService.update(other.getUsername(), updateDto())).isInstanceOf(ForbiddenException.class);
    }


    // UserService.get()
    @Test
    void cannotGetNonexistentUser() {
        String name = "tylerdurden";
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me);

        UserService userService = new UserServiceImpl(userRepository, null, null, null);
        assertThatThrownBy(() -> userService.get(name)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getGetsCorrectUser() {
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me);
        UserService userService = new UserServiceImpl(userRepository, null, null, null);
        UserDto userDto = userService.get(me.getUsername());

        assertThat(userDto).isNotNull();
        assertThat(userDto.username()).isEqualTo(me.getUsername());

        verify(userRepository).findByUsername(me.getUsername());
    }

    // UserService.delete()
    @Test
    void cannotDeleteOtherUser() {
        var me = testUser();
        var other = otherUser();
        UserRepository userRepository = mockUserRepository(me);
        UserService userService = new UserServiceImpl(userRepository, null, null, null);
        assertThatThrownBy(() -> userService.delete(other.getUsername())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void deleteCallsRepositoryForDeletion() {
        var me = testUser();
        UserRepository userRepository = mockUserRepository(me);
        UserService userService = new UserServiceImpl(userRepository, null, null, null);
        userService.delete(me.getUsername());

        verify(userRepository, times(1)).delete(me);
    }
}
