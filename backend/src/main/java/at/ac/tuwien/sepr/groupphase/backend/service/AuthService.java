package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

public interface AuthService {

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto);

    /**
     * Creates a new user and logs them in.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     *
     * @throws ValidationException if the user input is invalid
     * @throws ConflictException   if the user already exists
     */
    String create(UserCreateDto userLoginDto) throws ValidationException, ConflictException;
}
