package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

/**
 * Dto for creating a note component.
 */
public record NoteCreateDto(String title, List<LabelDto> labels, Long parentId, Long width, Long height, Long column, Long row) implements NoteDto {
}