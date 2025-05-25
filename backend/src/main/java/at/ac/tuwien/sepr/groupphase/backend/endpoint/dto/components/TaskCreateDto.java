package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.time.LocalDate;

public record TaskCreateDto(
        String name,
        Long parentId,
        long width,
        long height,
        long column,
        long row,
        LocalDate startDate,
        LocalDate endDate,
        boolean completed,
        boolean repeating
) implements TaskDto {
}
