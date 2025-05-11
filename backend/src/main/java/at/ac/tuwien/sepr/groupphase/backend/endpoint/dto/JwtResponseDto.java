package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record JwtResponseDto(
    String token
) {
    public JwtResponseDto(String token) {
        this.token = token;
    }
}
