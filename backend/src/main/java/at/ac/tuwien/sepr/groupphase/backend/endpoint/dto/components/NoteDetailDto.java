package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

/**
 * Dto for retrieving a note component.
 */
public record NoteDetailDto(long id, Long parentId, String name, List<LabelDto> labels, long width, long height, long column, long row, List<ComponentDetailDto> children) implements ComponentDetailDto {
}
