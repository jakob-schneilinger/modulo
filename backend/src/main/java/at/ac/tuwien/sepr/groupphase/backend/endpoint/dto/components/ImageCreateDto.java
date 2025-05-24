package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public record ImageCreateDto(Long parentId, long width, long height, long column, long row) implements ImageDto {
}
