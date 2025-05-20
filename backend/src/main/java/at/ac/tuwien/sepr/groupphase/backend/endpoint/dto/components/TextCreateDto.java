package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public record TextCreateDto(String text, Long parentId, long width, int fontSize, long height, long row, long column) implements TextDto {

}
