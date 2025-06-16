package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;

public record GroupDetailDto(Long id, String name, UserDto owner) {
}
