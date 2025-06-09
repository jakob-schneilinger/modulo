package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UrlDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.CalendarService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;
import net.fortuna.ical4j.data.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.management.ServiceNotFoundException;
import java.io.IOException;

/**
 * REST controller for managing component operations.
 * This controller provides endpoints for creating, updating, retrieving, and deleting components.
 * Specifically, it includes operations for managing boards within the application.
 */

@RestController
@RequestMapping(value = "/api/v1/component/calendar")
public class CalendarComponentEndpoint {

    private final CalendarService calendarService;

    @Autowired
    public CalendarComponentEndpoint(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    /**
     * Gets the details of the component with given id including all children.
     *
     * @param dto of the component to create
     * @return component detail of component
     */
    @PermitAll // TODO: fix this
    @PostMapping()
    public ResponseEntity<ComponentDetailDto> createCalendar(
        @RequestBody CalendarCreateDto dto
    ) {
        return new ResponseEntity<>(calendarService.createCalendar(dto), HttpStatus.CREATED);
    }

    /**
     * Set the details of the component with given id including all children.
     *
     * @param file of the component to set
     * @return component detail of component
     */
    @PermitAll
    @PutMapping("/file/{id}")
    public ResponseEntity<ComponentDetailDto> updateCalendarWithIcs(
            @PathVariable(name = "id") long id,
            @RequestPart("file") @NotNull MultipartFile file) {
        return new ResponseEntity<>(calendarService.updateCalendarIcs(id, file), HttpStatus.OK);

    }

    /**
     * Updates the URL of a Calendar.
     *
     * @param id the id of the Calendar to update
     * @param dto The URL dto of the calendar
     * @return the Updated calendar with the date from the URL
     */
    @PermitAll
    @PutMapping("/url/{id}")
    public ResponseEntity<ComponentDetailDto> updateCalendarWithUrl(
        @PathVariable(name = "id") long id,
        @RequestBody @Valid UrlDto dto) {
        System.out.println("urlDto:\n\n " + dto);
        return new ResponseEntity<>(calendarService.updateCalendarUrl(id, dto.url()), HttpStatus.OK);

    }


    /**
     * Clears the contents of the calendar.
     *
     * @param id the id of the calendar to be cleared
     * @return the detail representation of the cleared calendar
     */
    @PermitAll
    @PutMapping("/clear/{id}")
    public ResponseEntity<ComponentDetailDto> clearCalendar(
        @PathVariable(name = "id") long id
    ) {
        return new ResponseEntity<>(calendarService.clearCalendar(id), HttpStatus.OK);
    }

    /**
     * check if the external calendar has updated and if so updates the calendar.
     *
     * @param id the id of the calendar to update
     * @return ComponentDetailDto of the calendar that has been updated or null if no changes have been detected
     */
    @PermitAll
    @PutMapping("/refresh/{id}")
    public ResponseEntity<ComponentDetailDto> refreshCalendar(
        @PathVariable(name = "id") long id
    ) {
        return new ResponseEntity<>(calendarService.checkAndUpdateCalendar(id), HttpStatus.OK);
    }

}
