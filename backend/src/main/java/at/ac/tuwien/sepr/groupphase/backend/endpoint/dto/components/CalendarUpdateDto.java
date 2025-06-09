package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

public record CalendarUpdateDto(long id, Long parentId, Long width, Long height, Long column, Long row) implements CalendarDto {
}
