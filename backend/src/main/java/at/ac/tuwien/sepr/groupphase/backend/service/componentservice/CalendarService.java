package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.MyCalendar;
import net.fortuna.ical4j.data.ParserException;
import org.springframework.web.multipart.MultipartFile;

import javax.management.ServiceNotFoundException;
import java.io.IOException;
import java.net.UnknownServiceException;

public interface CalendarService {

    /**
     * Create the component for the calendar to be inserted.
     *
     * @param dto the calendar to be inserted.
     * @return the Detail representation of the calendar.
     */
    ComponentDetailDto createCalendar(CalendarCreateDto dto);

    /**
     * Insert the Calendar URL if non-existent or update otherwise.
     *
     * @param url the Url of the calendar.
     * @return the detail view of the updated calendar
     */
    ComponentDetailDto updateCalendarUrl(long id, String url);

    /**
     * Insert or update the ICS file of the calendar.
     *
     * @param file the ics file to update with
     * @return the detail view of the updated calendar.
     */
    ComponentDetailDto updateCalendarIcs(long id, MultipartFile file);

    /**
     * Clears the calendar the associated ics and link.
     *
     * @param id the id of the calendar to clear
     * @return A detail representation of the cleared calendar
     */
    ComponentDetailDto clearCalendar(long id);

    /**
     * Queries the provided ICal url in entity and updates the entity if remote changes have been detected.
     *
     * @param id the id of the calendar that should be refreshed
     * @return a CalendarDetailDto that reflects the remote changes
     */
    ComponentDetailDto checkAndUpdateCalendar(long id);
}
