package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for updating general component data.
 */
public record ComponentUpdateDto(long id, Long parentId, Long width, Long height, Long column, Long row) implements ComponentDto {
}