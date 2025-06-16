package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for creating a video component.
 */
public record VideoCreateDto(Long parentId, Long width, Long height, Long column, Long row) implements VideoDto {
}