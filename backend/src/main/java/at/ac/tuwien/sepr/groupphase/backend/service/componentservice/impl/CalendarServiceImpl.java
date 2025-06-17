package at.ac.tuwien.sepr.groupphase.backend.service.componentservice.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarEntryDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.CalendarEntry;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.MyCalendar;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.mapper.MappingDepth;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.CalendarService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.ComponentService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.validation.TaskValidator;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Service
public class CalendarServiceImpl implements CalendarService {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ComponentRepository componentRepository;
    private final ComponentService componentService;
    private final TaskService taskService;
    private final TaskValidator componentValidator;

    @Value("${global.location.calendar}")
    private String icsPath;
    private final HttpClient client;

    @Autowired
    public CalendarServiceImpl(ComponentRepository componentRepository,
                               ComponentService componentService, TaskService taskService, TaskValidator componentValidator) {
        this.componentRepository = componentRepository;
        this.componentService = componentService;
        this.taskService = taskService;
        this.client = HttpClient.newBuilder().build();
        this.componentValidator = componentValidator;
    }


    @Override
    public ComponentDetailDto createCalendar(CalendarCreateDto dto) {
        return componentService.setComponent(dto, new MyCalendar());
    }

    @Override
    @Transactional
    public ComponentDetailDto updateCalendarUrl(long id, String url) throws ValidationException, ConflictException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        byte[] data;
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                data = response.body();
            } else {
                throw new ValidationException(String.format("Unexpected response code: %s maybe your calendar is private: ", status), new ArrayList<>());
            }
        } catch (InterruptedException | IOException e) {
            throw new ConflictException("The server could not be reached", Arrays.asList(e.getMessage()));
        }

        storeIcs(id, data);
        MyCalendar calendar = getCalendar(id);
        Calendar ical = icalBuilder(data);
        calendar.getEntries().clear();
        calendar.getEntries().addAll(getCalendarEntries(ical));
        calendar.setIcalUrl(url);
        componentService.setComponent(new CalendarUpdateDto(id, null, null, null, null, null), calendar);
        return calendar.accept(MappingDepth.SHALLOW);
    }


    @Override
    @Transactional
    public ComponentDetailDto updateCalendarIcs(long id, MultipartFile file) throws ValidationException {
        MyCalendar calendar = getCalendar(id);
        try {
            storeIcs(id, file.getBytes());
            Calendar ical = icalBuilder(file.getBytes());
            if (calendar.getIcalUrl() != null) {
                calendar.setIcalUrl(null);
            }
            calendar.getEntries().clear();
            calendar.getEntries().addAll(getCalendarEntries(ical));
            componentService.setComponent(new CalendarUpdateDto(id, null, null, null, null, null), calendar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return calendar.accept(MappingDepth.SHALLOW);

    }

    @Override
    @Transactional
    public ComponentDetailDto clearCalendar(long id) {
        MyCalendar calendar = getCalendar(id);
        calendar.getEntries().clear();
        calendar.setIcalUrl(null);
        componentService.setComponent(new CalendarUpdateDto(id, null, null, null, null, null), calendar);
        Path icalPath = Paths.get(icsPath);
        Path targetPath = icalPath.resolve(id + ".ics");
        try {
            Files.delete(targetPath);
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }

        return calendar.accept(MappingDepth.SHALLOW);
    }

    @Override
    @Transactional
    public ComponentDetailDto checkAndUpdateCalendar(long id) {
        MyCalendar calendar = getCalendar(id);
        if (calendar.getIcalUrl() == null) {
            throw new NotFoundException("No Link has been given for this calendar!");
        }
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(calendar.getIcalUrl()))
            .GET()
            .build();
        byte[] data = new byte[0];
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                data = response.body();
            } else {
                throw new ValidationException(String.format("Unexpected response code: %s maybe your calendar is private: ", status), new ArrayList<>());
            }
        } catch (IOException | InterruptedException e) {
            LOG.info(e.getMessage());
        }

        Calendar ical = icalBuilder(data);
        storeIcs(calendar.getId(), data);
        List<CalendarEntry> tasks = calendar.getEntries().stream().filter(e -> e.isFromTask()).toList();
        calendar.getEntries().clear();
        calendar.getEntries().addAll(getCalendarEntries(ical));
        calendar.getEntries().addAll(tasks);
        componentService.setComponent(new CalendarUpdateDto(id, null, null, null, null, null), calendar);
        return calendar.accept(MappingDepth.SHALLOW);
    }

    @Override
    @Transactional
    public ComponentDetailDto addTaskToCalendar(long id, TaskDetailDto dto) {
        componentValidator.validateForCalendar(dto);
        CalendarEntry toAdd = new CalendarEntry();
        MyCalendar calendar = getCalendar(id);
        toAdd.setTitle(dto.name());
        toAdd.setStartDate(dto.startDate() != null ?  dto.startDate() : dto.endDate());
        toAdd.setEndDate(dto.endDate());
        toAdd.setFromTask(true);
        List<CalendarEntry> entries = calendar.getEntries();
        entries.add(toAdd);
        calendar.setEntries(entries);
        componentService.setComponent(new CalendarUpdateDto(id, null, null, null, null, null), calendar);
        return calendar.accept(MappingDepth.SHALLOW);
    }

    @Transactional
    @Override
    public ComponentDetailDto deleteEntry(long id, CalendarEntryDetailDto dto) {
        MyCalendar calendar = getCalendar(id);
        List<CalendarEntry> entries = calendar.getEntries();
        entries = entries.stream().filter(e -> e.getId() != dto.id()).toList();
        calendar.getEntries().clear();
        calendar.getEntries().addAll(entries);
        componentService.setComponent(new CalendarUpdateDto(id, null, null, null, null, null), calendar);
        return calendar.accept(MappingDepth.SHALLOW);
    }

    private void storeIcs(long calendarId, byte[] file) {
        File dir = new File(icsPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Unable to create directory " + icsPath);
            }
        }
        try (var in = new ByteArrayInputStream(file)) {
            Path icalPath = Paths.get(icsPath);
            Files.createDirectories(icalPath);
            Path target = icalPath.resolve(calendarId + ".ics");
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to store ics file");
        }
    }

    private Calendar icalBuilder(byte[] file) throws ValidationException {
        try {
            InputStream inputStream = new ByteArrayInputStream(file);
            CalendarBuilder builder = new CalendarBuilder();
            Calendar ical = builder.build(inputStream);
            ical.validate();
            return ical;
        } catch (ParserException e) {
            throw new ValidationException("Unable to parse ics file", Arrays.asList(e.getMessage()));
        } catch (IOException e) {
            throw new RuntimeException("Unable to build calender from ics file");
        }
    }

    private List<CalendarEntry> getCalendarEntries(Calendar calendar) {

        //these are the components in the iCal calendar, not to be confused with the calendar component!
        List<CalendarComponent> comps = calendar.getComponents().stream().filter(comp -> comp.getName().equals("VEVENT")).toList();
        List<CalendarEntry> entries = new ArrayList<>();
        for (CalendarComponent comp : comps) {
            VEvent vevent = (VEvent) comp;
            if (vevent == null) {
                throw new ValidationException("No VEVENT found", Arrays.asList("calendar contains no events"));
            }

            Summary summaryProp = vevent.getSummary();
            Description descriptionProp = vevent.getDescription();
            DtStart<Temporal> dtStartProp = vevent.getDateTimeStart();
            DtEnd<Temporal> dtEndProp = vevent.getDateTimeEnd();  // may be null for all-day or duration events
            RRule<Temporal> rule = (RRule<Temporal>) vevent.getProperty(Property.RRULE).orElse(null);

            Temporal start = castTemporal(dtStartProp);
            Temporal end = castTemporal(dtEndProp);

            CalendarEntry entry = new CalendarEntry();
            entry.setTitle(summaryProp != null ? summaryProp.getValue() : "(no title)");
            entry.setDescription(descriptionProp != null ? descriptionProp.getValue() : null);
            entry.setStartDate(LocalDate.from(start));
            entry.setEndDate(end != null ? LocalDate.from(end) : LocalDate.from(start));
            entry.setRrule(rule != null ? rule.getValue() : null);
            entries.add(entry);
        }

        return entries;
    }

    private Temporal castTemporal(DateProperty<Temporal> dateProperty) {
        Temporal temp = null;
        if (dateProperty != null) {
            temp = dateProperty.getDate();
            if (temp instanceof LocalDate) {
                temp = LocalDate.from(temp);
            }
            if (temp instanceof Instant in) {
                temp = in.atZone(ZoneId.of(dateProperty.getParameter(Property.TZID).orElse(new Parameter("") {
                    @Override
                    public String getValue() {
                        return "Europe/Vienna";
                    }
                }).getValue())).toLocalDate();
            }
        }
        return temp;
    }

    private MyCalendar getCalendar(long id) {
        at.ac.tuwien.sepr.groupphase.backend.entity.components.Component component = componentRepository.findById(id).orElseThrow(() -> new NotFoundException("Calendar with given ID does not exist"));
        MyCalendar calendar = (MyCalendar) component;
        return calendar;
    }
}