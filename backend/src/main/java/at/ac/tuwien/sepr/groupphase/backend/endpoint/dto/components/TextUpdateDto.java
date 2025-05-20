package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;


public record TextUpdateDto(long id, String name, long width, String text, Long parentId, int fontSize, long height, long row, long column) implements TextDto {
}
