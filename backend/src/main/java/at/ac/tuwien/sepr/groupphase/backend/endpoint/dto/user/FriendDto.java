package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user;

public record FriendDto(String username, String displayName, String email, String requesterName, boolean accepted) {
}
