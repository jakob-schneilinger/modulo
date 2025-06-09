package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.time.LocalDate;

public record CalendarEntryDetailDto(LocalDate startDate, LocalDate endDate, String title, String description) {
}
