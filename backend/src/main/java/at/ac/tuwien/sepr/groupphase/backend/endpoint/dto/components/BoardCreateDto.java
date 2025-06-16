package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

/**
 * Dto for creating a board component.
 */
public record BoardCreateDto(String name, Integer depth, Long parentId, Long width, Long height, Long column, Long row) implements BoardDto {
}