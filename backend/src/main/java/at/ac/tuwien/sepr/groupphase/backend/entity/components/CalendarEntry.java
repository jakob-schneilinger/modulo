package at.ac.tuwien.sepr.groupphase.backend.entity.components;


import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarEntryDetailDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.model.property.RRule;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "calendar_entries")
public class CalendarEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String title;

    @Column(columnDefinition = "text")
    private String description;

    private String rrule;



}


