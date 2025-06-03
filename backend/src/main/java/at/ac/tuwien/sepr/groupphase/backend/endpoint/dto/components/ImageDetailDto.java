package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for retrieving an image component.
 */
public record ImageDetailDto(long id, long width, long height, long column, long row) implements ComponentDetailDto {
}