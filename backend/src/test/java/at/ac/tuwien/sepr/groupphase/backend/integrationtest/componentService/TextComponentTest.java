package at.ac.tuwien.sepr.groupphase.backend.integrationtest.componentService;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Board;
import at.ac.tuwien.sepr.groupphase.backend.entity.components.Text;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ComponentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.BoardService;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired TextService textService;
    @Autowired UserRepository userRepository;
    @Autowired ComponentRepository componentRepository;

    private ApplicationUser user;
    @Autowired
    private BoardService boardService;

    private TextCreateDto createDto(long parentId){
        return new TextCreateDto(
            "Hello World",
            parentId,
            1L,
            1L,
            1L,
            1L
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
        BoardCreateDto root = new BoardCreateDto(
            "name", null, null, null, null, null, null
        );

        ComponentDetailDto detail = boardService.createBoard(root);

        TextCreateDto dto = new TextCreateDto(
            "Hello World",
            detail.id(),
            1L, 1L, 1L, 1L
        );

        TextDetailDto created = (TextDetailDto) textService.createTextComponent(dto);
        assertThat(created.content()).isEqualTo("Hello World");
        assertThat(componentRepository.findById(created.id())).isPresent();
    }

    @Test
    @Transactional
    void updateTextComponent_updatesExistingEntity() {
        BoardCreateDto root = new BoardCreateDto(
            "name", null, null, null, null, null, null
        );

        ComponentDetailDto detail = boardService.createBoard(root);

        TextCreateDto dto = createDto(detail.id());

        TextDetailDto created = (TextDetailDto) textService.createTextComponent(dto);

        TextUpdateDto updateDto = new TextUpdateDto(created.id(), "textbox", detail.id(), 100L, 1L, 1L, 1L);
        TextDetailDto update = (TextDetailDto) textService.updateTextComponent(updateDto);
        assertThat(update.content()).isEqualTo("textbox");
    }

    @Test
    @Transactional
    void updateTextComponent_userNotOwner_throwsRuntimeException() {

        ApplicationUser other = new ApplicationUser();
        other.setUsername("Other");
        other.setEmail("pray@ahhh.ah");
        other.setDisplayName("other User");
        other = userRepository.save(other);

        Board root = new Board();
        root.setBoardName("testBoard");
        root.setOwnerId(user.getId());
        root.setDepth(5);
        root.setColumn(0L);
        root.setWidth(0L);
        root.setRow(0L);
        root.setHeight(0L);
        root = componentRepository.save(root);

        Text text = new Text(); text.setContent("text"); text.setWidth(50L); text.setOwnerId(user.getId()); text.setHeight(1L); text.setWidth(1L);
        text.setColumn(1L);
        text.setRow(1L);
        text.setParents(List.of(root));

        text = componentRepository.save(text);

        var auth = new UsernamePasswordAuthenticationToken(
            other.getUsername(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);


        TextUpdateDto dto = new TextUpdateDto(
            text.getId(),
            "textbox",
            root.getId(),
            50L,
            1L,
            1L,
            1L
        );

        // when / then
        assertThatThrownBy(() -> textService.updateTextComponent(dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User is not authorized");
    }


    @Test
    @Transactional
    void updateTextComponent_notFound_throwsNotFoundException() {
        TextUpdateDto dto = new TextUpdateDto(
            999L,
            "some text",
            null,
            50L,
            1L,
            1L,
            1L
        );
        assertThatThrownBy(() -> textService.updateTextComponent(dto))
            .isInstanceOf(NotFoundException.class);
    }
}
