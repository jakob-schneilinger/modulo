package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtUtils;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final JwtUtils utils;

    public UserServiceImpl(UserRepository userRepository, JwtUtils utils) {
        this.userRepository = userRepository;
        this.utils = utils;
    }

    @Override
    public void update(UserUpdateDto userUpdateDto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(String username, String token) {
        LOGGER.trace("delete user: {}", username);

        String subject = utils.getUsernameFromJwtToken(token);
        if (!subject.equals(username)) {
            throw new ForbiddenException("You don't have permissions to delete this user");
        }

        Optional<ApplicationUser> user = userRepository.findByUsername(username);

        // should never be a conflict -> user has to exist (token)
        if (user == null || !user.isPresent()) {
            throw new ConflictException("User doesn't exist", new LinkedList<>());
        }

        userRepository.delete(user.get());
    }

}
