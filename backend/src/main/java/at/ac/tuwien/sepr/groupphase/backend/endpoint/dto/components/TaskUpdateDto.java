package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.time.LocalDate;
import java.util.List;

public record TaskUpdateDto(
    long id,
    String name,
    long width,
    long height,
    long column,
    long row,
    List<ComponentDetailDto> children,
    LocalDate startDate,
    LocalDate endDate,
    boolean completed,
    boolean repeating,
    Long parentId
) implements TaskDto{
}
