package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.CalendarDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.MyCalendar;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.CalendarService;
import net.fortuna.ical4j.data.ParserException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
public class CalendarComponentTests {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ComponentRepository componentRepository;
    @Autowired
    CalendarService calendarService;
    private ApplicationUser user;


    @BeforeEach
    @Transactional
    void prepareSecurityContext() {
        if (user == null) {
            user = new ApplicationUser();
            user.setUsername("testUser01");
            user.setEmail("ah@ahhh.ah");
            user.setDisplayName("Test User");
            user = userRepository.save(user);
        } else {
            user = userRepository.findByUsername("testUser01").orElseThrow();
        }

        var auth = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

    }

    private CalendarCreateDto createDto() {
        return new CalendarCreateDto("calendar-Create", null, 1L, 1L, 1L, 1L );
    }

    @Transactional
    @Test
    void createCalendar_persistsEntity() {
        CalendarCreateDto dto = createDto();
        CalendarDetailDto calendar = (CalendarDetailDto) calendarService.createCalendar(dto);
        assertThat(componentRepository.findById(calendar.id())).isPresent();
    }


    @Nested
    @DisplayName("Write-Delete Tests")
    @Transactional
    class WriteDeleteTests {

        @AfterAll
        static void cleanup() throws IOException {
            Path imagePath = Path.of("res/test_calendar");
            if (Files.exists(imagePath)) {
                List<Path> paths = Files.walk(imagePath).sorted(Comparator.reverseOrder()).toList();
                for (Path path : paths) {
                    Files.deleteIfExists(path);
                }
            }
        }

        @Test
        void updateCalendarIcs_updatesEntity_returnsDto() throws IOException, ParserException {
            CalendarCreateDto dto = createDto();
            CalendarDetailDto calendar = (CalendarDetailDto) calendarService.createCalendar(dto);
            byte[] dummyIcs = Files.readAllBytes(
                Path.of("src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittest/modulosepm@gmail.com.ics"));
            MockMultipartFile filePart = new MockMultipartFile(
                "calendar",
                dummyIcs);

            CalendarDetailDto caldto = (CalendarDetailDto) calendarService.updateCalendarIcs(calendar.id(), filePart);
            assertThat(componentRepository.findById(caldto.id())).isPresent();
            assertThat(caldto.id()).isEqualTo(calendar.id());
            assertEquals(20, caldto.entries().size());
        }

        @Test
        void clearCalendar_clearsEntries_returnsDto() throws IOException, ParserException {
            CalendarCreateDto dto = createDto();
            CalendarDetailDto calendar = (CalendarDetailDto) calendarService.createCalendar(dto);
            byte[] dummyIcs = Files.readAllBytes(
                Path.of("src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittest/modulosepm@gmail.com.ics"));
            MockMultipartFile filePart = new MockMultipartFile(
                "calendar",
                dummyIcs);
            CalendarDetailDto caldto = (CalendarDetailDto) calendarService.updateCalendarIcs(calendar.id(), filePart);

            calendarService.clearCalendar(caldto.id());
            MyCalendar calendar2 = (MyCalendar) componentRepository.findById(caldto.id()).orElseThrow();
            assertEquals(0, calendar2.getEntries().size());
            assertFalse(Files.exists(Path.of("res/test_calendar/" + calendar2.getId() + ".ics")));
        }
    }


}
