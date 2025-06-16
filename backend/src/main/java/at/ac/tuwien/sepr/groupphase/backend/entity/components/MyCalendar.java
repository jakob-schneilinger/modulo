package at.ac.tuwien.sepr.groupphase.backend.entity.components;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarEntryDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.mapper.ComponentEntityToDtoMapper;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.model.Recur;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Entity
@Table(name = "calendar_content")
@DiscriminatorValue("calendar")
@Getter
@Setter
@PrimaryKeyJoinColumn(name = "id")
public class MyCalendar extends Component {

    @Override
    public ComponentDetailDto accept(MappingDepth depth) {
        return ComponentEntityToDtoMapper.visit(this);
    }

    @OneToMany(
        cascade       = CascadeType.ALL,
        orphanRemoval = true,
        fetch         = FetchType.LAZY
    )
    @JoinColumn(
        name               = "container_id",
        referencedColumnName = "id",
        nullable           = false
    )
    private List<CalendarEntry> entries;

    @Column(name = "ical_url")
    private String icalUrl;

    private String etag;

    public static List<CalendarEntryDetailDto> getCalendarEntries(List<CalendarEntry> calendar) {
        List<CalendarEntryDetailDto> calendarDetails = new ArrayList<>();
        for (CalendarEntry entry : calendar) {
            if (entry.getRrule() != null) {
                Recur<LocalDate> recur = new Recur<>(entry.getRrule());
                LocalDate seed = entry.getStartDate();
                List<LocalDate> recurringEvents = recur.getDates(seed, LocalDate.now(), LocalDate.now().plusYears(1), 10);
                calendarDetails.addAll(
                    recurringEvents.stream()
                        .map(startDate ->
                            new CalendarEntryDetailDto(startDate,
                                startDate.plus(Period.between(entry.getStartDate(),
                                    entry.getEndDate())), entry.getTitle(),
                                entry.getDescription()))
                        .toList());
            } else {
                calendarDetails.add(new CalendarEntryDetailDto(entry.getStartDate(), entry.getEndDate(), entry.getTitle(), entry.getDescription()));
            }
        }

        calendarDetails.sort(Comparator.comparing(CalendarEntryDetailDto::startDate));
        return calendarDetails;
    }
}

