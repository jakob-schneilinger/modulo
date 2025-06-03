package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.time.LocalDate;

public record TaskCreateDto(
        String name,
        Long parentId,
        Long width,
        Long height,
        Long column,
        Long row,
        LocalDate startDate,
        LocalDate endDate,
        boolean completed,
        boolean repeating) implements TaskDto {
}
