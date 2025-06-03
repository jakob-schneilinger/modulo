package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for creating an image component.
 */
public record ImageCreateDto(Long parentId, Long width, Long height, Long column, Long row) implements ImageDto {
}