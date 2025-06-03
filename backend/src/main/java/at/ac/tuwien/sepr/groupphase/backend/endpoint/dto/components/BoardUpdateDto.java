package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for updating a board component.
 */
public record BoardUpdateDto(long id, String name, Long parentId, Long width, Long height, Long column, Long row) implements BoardDto {
}