package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for retrieving a text component.
 */
public record TextDetailDto(long id, String content, long width, long height, long column, long row) implements ComponentDetailDto {
}