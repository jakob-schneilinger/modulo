package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for updating an image component.
 */
public record ImageUpdateDto(long id, Long parentId, Long width, Long height, Long column, Long row)
        implements ImageDto {
}