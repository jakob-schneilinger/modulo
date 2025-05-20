package at.ac.tuwien.sepr.groupphase.backend.unittest;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ComponentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest
public class TextComponentTest {

    @Autowired ComponentServiceImpl componentService;
    @Autowired UserRepository userRepository;
    @Autowired ComponentRepository componentRepository;

    private ApplicationUser user;


    private TextCreateDto createDto(){
        return new TextCreateDto(
            "Hello World",
            null,
            100,
            12,
            1, 1, 1
        );
    }

    @BeforeEach
    @Transactional
    void prepareSecurityContext() {

        if(user==null) {
            user = new ApplicationUser();
            user.setUsername("testUser01");
            user.setEmail("ah@ahhh.ah");
            user.setDisplayName("Test User");
            user = userRepository.save(user);
        }else {
            user = userRepository.findByUsername("testUser01").orElseThrow();
        }

            var auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);


    }

    @Test
    @Transactional
    void createTextComponent_persistsEntity() {
        TextCreateDto dto = new TextCreateDto(
            "Hello World",
            null,
            100,
            12,
            1, 1, 1
        );

        TextDetailDto created = (TextDetailDto) componentService.createTextComponent(dto);
        assertThat(created.text()).isEqualTo("Hello World");
        assertThat(componentRepository.findById(created.id())).isPresent();
    }



    @Test
    @Transactional
    void updateTextComponent_updatesExistingEntity() {

        TextCreateDto dto = createDto();

        TextDetailDto created = (TextDetailDto) componentService.createTextComponent(dto);

        TextUpdateDto updateDto = new TextUpdateDto(created.id(), null, 100, "Updated", null,12,1L,1L,1L);
        TextDetailDto update = (TextDetailDto) componentService.updateTextComponent(updateDto);
        assertThat(update.text()).isEqualTo("Updated");
    }


    @Test
    @Transactional
    void updateTextComponent_userNotOwner_throwsRuntimeException() {

        ApplicationUser other = new ApplicationUser();
        other.setUsername("Other");
        other.setEmail("pray@ahhh.ah");
        other.setDisplayName("other User");
        other = userRepository.save(other);

        Text text = new Text(); text.setWidth(50L); text.setText("Some text"); text.setFontSize(12); text.setOwnerId(user.getId()); text.setHeight(1L); text.setWidth(1L);
        text.setColumn(1L);
        text.setRow(1L);
        text = componentRepository.save(text);

        var auth = new UsernamePasswordAuthenticationToken(
            other.getUsername(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        TextUpdateDto dto = new TextUpdateDto(
            text.getId(),
            null,
            50L,
            "New text",
            null,
            12,
            1L,
            1L,
            1L
        );

        // when / then
        assertThatThrownBy(() -> componentService.updateTextComponent(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User is not owner");
    }


    @Test
    @Transactional
    void updateTextComponent_notFound_throwsNotFoundException() {
        TextUpdateDto dto = new TextUpdateDto(
            999L,
            "some text",
            50L,
            "New text",
            null,
            12,
            1L,
            1L,
            1L
        );
        assertThatThrownBy(() -> componentService.updateTextComponent(dto))
            .isInstanceOf(NotFoundException.class);
    }


}
