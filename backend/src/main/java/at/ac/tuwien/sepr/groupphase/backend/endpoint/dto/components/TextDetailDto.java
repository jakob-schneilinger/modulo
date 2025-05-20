package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public record TextDetailDto(String text, long id, long width, long height, long column, long row, int fontSize) implements ComponentDetailDto {
}