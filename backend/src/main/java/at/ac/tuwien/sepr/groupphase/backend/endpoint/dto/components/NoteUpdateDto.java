package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

/**
 * Dto for updating an image component.
 */
public record NoteUpdateDto(long id, String title, List<LabelDto> labels, Long parentId, Long width, Long height, Long column, Long row) implements NoteDto {
}