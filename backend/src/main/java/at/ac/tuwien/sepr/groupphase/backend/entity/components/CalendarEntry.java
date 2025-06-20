package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents a calendar entry entity.
 */
@Entity
@Getter
@Setter
@Table(name = "calendar_entries")
public class CalendarEntry {

    /**
     * ID of this entry.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Start date of this entry.
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * End date of this entry.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Title of this entry.
     */
    private String title;

    /**
     * Description of this entry.
     */
    @Column(columnDefinition = "text")
    private String description;

    /**
     * Recurrence rule based on the i-cal file of this entry.
     */
    private String rrule;

    /**
     * Boolean which signals if this entry comes from a task or extern.
     */
    @Column(name = "from_task")
    private boolean fromTask;
}