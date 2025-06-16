package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for updating an video component.
 */
public record VideoUpdateDto(long id, Long parentId, Long width, Long height, Long column, Long row)
        implements VideoDto {
}