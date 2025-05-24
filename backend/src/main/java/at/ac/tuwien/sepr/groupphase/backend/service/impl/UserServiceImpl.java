package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.validation.UserValidator;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${global.location.avatars}")
    private String avatarPath;

    private final UserValidator validator;
    private final UserRepository userRepository;
    private final JwtUtils utils;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, JwtUtils utils, UserValidator validator,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.utils = utils;
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
    }

    private String currentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Override
    public void update(String username, UserUpdateDto userUpdateDto) {
        LOGGER.trace("update user: {} - {}", username, userUpdateDto);

        validator.validateUserUpdate(userUpdateDto);

        Optional<ApplicationUser> user = userRepository.findByUsername(username);

        if (user == null || !user.isPresent()) {
            throw new NotFoundException("User doesn't exist");
        }

        ApplicationUser toUpdate = user.get();

        if (userUpdateDto.displayName() != null) {
            toUpdate.setDisplayName(userUpdateDto.displayName());
        }
        if (userUpdateDto.email() != null) {
            toUpdate.setEmail(userUpdateDto.email());
        }
        if (userUpdateDto.password() != null) {
            toUpdate.getPassword()
                    .setHash(passwordEncoder.encode(userUpdateDto.password() + toUpdate.getSalt().getSalt()));
        }

        userRepository.save(toUpdate);
    }

    @Override
    public void delete(String username) {
        LOGGER.trace("delete user: {}", username);

        if (!currentUserName().equals(username)) {
            throw new ForbiddenException("You don't have permissions to delete this user");
        }

        Optional<ApplicationUser> user = userRepository.findByUsername(username);

        // should never be a conflict -> user has to exist (token)
        if (user == null || !user.isPresent()) {
            throw new ConflictException("User doesn't exist", new LinkedList<>());
        }

        userRepository.delete(user.get());
    }

    @Override
    public UserDto get(String username) {
        LOGGER.trace("get user: {}", username);

        Optional<ApplicationUser> user = userRepository.findByUsername(username);

        if (user == null || !user.isPresent()) {
            throw new NotFoundException("User doesn't exist");
        }

        // omit email if looking at other users
        if (!currentUserName().equals(username)) {
            return new UserDto(user.get().getUsername(), user.get().getDisplayName(), null);
        }
        return new UserDto(user.get().getUsername(), user.get().getDisplayName(), user.get().getEmail());
    }

    @Override
    public void saveAvatar(String username, byte[] image) {
        LOGGER.trace("save avatar of: {}", username);

        if (!currentUserName().equals(username)) {
            throw new ForbiddenException("You don't have permissions to change this users avatar");
        }

        try {
            // write to system
            File file = new File(avatarPath, username);
            var stream = new FileOutputStream(file);
            stream.write(image);
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't store file. This should not happen. Please contact an administrator.");
        }
    }

    @Override
    public void removeAvatar(String username) {
        if (!currentUserName().equals(username)) {
            throw new ForbiddenException("You don't have permissions to delete this users avatar");
        }

        File file = new File(avatarPath, username);
        file.delete();
    }
}
