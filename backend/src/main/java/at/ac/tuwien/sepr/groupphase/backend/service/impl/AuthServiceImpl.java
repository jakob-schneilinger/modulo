package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Password;
import at.ac.tuwien.sepr.groupphase.backend.entity.Salt;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthService;
import at.ac.tuwien.sepr.groupphase.backend.validation.UserValidator;

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
    private final UserValidator validator;

    @Autowired
    public AuthServiceImpl(UserValidator validator, UserRepository userRepository, PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.validator = validator;
    }

    private Authentication buildAuthentication(String username, String password, String salt) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password + salt));
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        LOGGER.debug("Log in user: {}", userLoginDto.toString());

        Optional<ApplicationUser> user = userRepository.findByUsername(userLoginDto.username());

        if (!user.isPresent()) {
            throw new BadCredentialsException("Username or Password wrong!");
        }

        Authentication authentication = buildAuthentication(
                userLoginDto.username(),
                userLoginDto.password(),
                user.get().getSalt().getSalt());

        return jwtUtils.generateJwtToken(authentication);
    }

    @Override
    public String create(UserCreateDto userCreateDto) {
        LOGGER.debug("Create user: {}", userCreateDto.toString());

        validator.validateUserCreate(userCreateDto);

        ApplicationUser user = new ApplicationUser();

        Password pwd = new Password();
        Salt salt = new Salt();
        salt.setSalt(Salt.generate());
        salt.setUser(user);

        pwd.setHash(passwordEncoder.encode(userCreateDto.getPassword() + salt.getSalt()));
        pwd.setUser(user);

        user.setUsername(userCreateDto.getUsername());
        user.setDisplayName(userCreateDto.getDisplayName());
        user.setEmail(userCreateDto.getEmail());
        user.setPassword(pwd);
        user.setSalt(salt);

        userRepository.save(user);

        Authentication authentication = buildAuthentication(
                user.getUsername(),
                userCreateDto.getPassword(),
                salt.getSalt());

        return jwtUtils.generateJwtToken(authentication);
    }
}
