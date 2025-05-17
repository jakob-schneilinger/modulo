package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public record BoardCreateDto(String name, Long parentId, long width, long height, long column, long row) implements BoardDto {
}
