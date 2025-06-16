package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

public record CalendarDetailDto(long id, Long parentId, long width, long height, long column, long row, List<CalendarEntryDetailDto> entries) implements ComponentDetailDto {
}
