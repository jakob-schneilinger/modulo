package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.time.LocalDate;

public record CalendarCreateDto(
    String name,
    Long parentId,
    Long width,
    Long height,
    Long column,
    Long row
) implements CalendarDto {
}
