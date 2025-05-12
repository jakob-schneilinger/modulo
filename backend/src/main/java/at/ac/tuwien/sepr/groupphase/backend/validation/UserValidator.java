package at.ac.tuwien.sepr.groupphase.backend.validation;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

@Component
public class UserValidator {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private static final Pattern emailPattern = Pattern
            .compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static final Pattern usernamePattern = Pattern
            .compile("^[a-z0-9._-]+$", Pattern.CASE_INSENSITIVE);

    public UserValidator(UserRepository userRepository /* PasswordEncoder passwordEncoder */) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void validateUserCreate(UserCreateDto createDto) {
        List<String> validations = new LinkedList<>();

        // User name validation
        if (createDto.getUsername() != null && !createDto.getUsername().isEmpty()) {
            if (createDto.getUsername().length() > 200) {
                validations.add("Username to long");
            }
            if (!usernamePattern.matcher(createDto.getUsername()).matches()) {
                validations.add("Username doesn't meet requirements");
            }
        } else {
            validations.add("Username required");
        }

        // Display name validation
        if (createDto.getDisplayName() != null && !createDto.getDisplayName().isEmpty()) {
            if (createDto.getDisplayName().length() > 200) {
                validations.add("Display name to long");
            }
        }

        // Email validation
        if (createDto.getEmail() != null && !createDto.getEmail().isEmpty()) {
            if (!emailPattern.matcher(createDto.getEmail()).matches()) {
                validations.add("Invalid email format");
            }
            if (createDto.getEmail().length() > 200) {
                validations.add("Email to long");
            }

        } else {
            validations.add("Email required");
        }

        // Password validation
        if (createDto.getPassword() != null && !createDto.getPassword().isEmpty()) {
            if (createDto.getPassword().length() < 8) {
                validations.add("Password must be at least 8 characters");
            }
            if (createDto.getPassword().length() > 200) {
                validations.add("Password to long");
            }
        } else {
            validations.add("Password required");
        }

        if (!validations.isEmpty()) {
            throw new ValidationException("Validation of user failed", validations);
        }

        List<String> conflicts = new LinkedList<>();
        if (userRepository.existsByUsername(createDto.getUsername())) {
            conflicts.add("Username already taken");
        }

        if (userRepository.existsByEmail(createDto.getEmail())) {
            conflicts.add("Email already taken");
        }

        if (!conflicts.isEmpty()) {
            throw new ConflictException("Conflicts in user data", conflicts);
        }
    }
}
