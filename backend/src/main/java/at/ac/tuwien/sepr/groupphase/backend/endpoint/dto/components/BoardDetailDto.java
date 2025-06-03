package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

/**
 * Dto for retrieving a board component.
 */
public record BoardDetailDto(long id, String name, long width, long height, long column, long row, List<ComponentDetailDto> children) implements ComponentDetailDto {
}