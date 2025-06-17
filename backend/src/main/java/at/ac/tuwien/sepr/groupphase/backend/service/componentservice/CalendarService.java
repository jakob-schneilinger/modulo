package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarEntryDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDetailDto;
import org.springframework.web.multipart.MultipartFile;

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


    /**
     * Adds the task to the calendar specified in id.
     *
     * @param id the id of the calendar to which the task should be added
     * @param dto the task to add to the calendar
     * @return the updated calendar
     */
    ComponentDetailDto addTaskToCalendar(long id, TaskDetailDto dto);

    /**
     * Delete the entry from the calendar.
     *
     * @param id the calendar from witch to delete
     * @param dto the entry to delete
     * @return the updated calendar
     */
    ComponentDetailDto deleteEntry(long id, CalendarEntryDetailDto dto);
}

