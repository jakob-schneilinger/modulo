package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for updating a text component.
 */
public record TextUpdateDto(long id, String content, Long parentId, Long width, Long height, Long row, Long column) implements TextDto {
}
