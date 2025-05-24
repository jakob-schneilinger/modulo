package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public record ComponentUpdateDto(long id, Long parentId, long width, long height, long column, long row) implements ComponentDto {
}
