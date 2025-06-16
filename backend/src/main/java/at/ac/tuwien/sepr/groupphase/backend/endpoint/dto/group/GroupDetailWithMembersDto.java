package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;

import java.util.Set;

public record GroupDetailWithMembersDto(Long id, String name, UserDto owner, Set<UserDto> members) {
}
