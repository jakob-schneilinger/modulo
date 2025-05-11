package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Password;
import at.ac.tuwien.sepr.groupphase.backend.entity.Salt;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import jakarta.security.auth.message.AuthException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        LOGGER.debug("Log in user: {}", userLoginDto.toString());

        Optional<ApplicationUser> user = userRepository.findByUsername(userLoginDto.username());

        if (!user.isPresent()) {
            throw new BadCredentialsException("Username or Password wrong!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginDto.username(),
                        userLoginDto.password() + user.get().getSalt().getSalt()));

        return jwtUtils.generateJwtToken(authentication);
    }

    @Override
    public void create(UserCreateDto userCreateDto) throws ConflictException, ValidationException {
        // TODO: move to validator
        if (userCreateDto.getEmail() == null || !userCreateDto.getEmail().contains("@")) {
            throw new ValidationException("Invalid email format");
        }
        if (userCreateDto.getPassword() == null || userCreateDto.getPassword().length() < 8) {
            throw new ValidationException("Password must be at least 8 characters.");
        }

        if (userRepository.existsByUsername(userCreateDto.getUsername())) {
            throw new ConflictException("Username already taken!");
        }

        ApplicationUser user = new ApplicationUser();

        Password pwd = new Password();
        Salt salt = new Salt();
        salt.setSalt(Salt.generate());
        salt.setUser(user);

        pwd.setHash(passwordEncoder.encode(userCreateDto.getPassword() + salt.getSalt()));
        pwd.setUser(user);

        user.setUsername(userCreateDto.getUsername());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(pwd);
        user.setSalt(salt);

        userRepository.save(user);
    }
}
