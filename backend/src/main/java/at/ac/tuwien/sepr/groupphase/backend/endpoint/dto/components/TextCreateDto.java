package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for creating a text component.
 */
public record TextCreateDto(String content, Long parentId, Long width, Long height, Long row, Long column)
                implements TextDto {
}
