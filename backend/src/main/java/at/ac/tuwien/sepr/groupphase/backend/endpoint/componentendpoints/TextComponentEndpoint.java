package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TextService;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1/component/text")
public class TextComponentEndpoint {

    private final TextService service;

    @Autowired
    public TextComponentEndpoint(TextService textService) {
        this.service = textService;
    }

    /**
     * Creates a text component in the database.
     *
     * @param textComponent information to add
     * @return component detail of created text component
     */
    @PermitAll
    @PostMapping("")
    public ResponseEntity<ComponentDetailDto> createTextComponent(
        @RequestBody TextCreateDto textComponent) {
        return new ResponseEntity<>(service.createTextComponent(textComponent), HttpStatus.CREATED);
    }

    /**
     * Updates a text component in the database.
     *
     * @param textComponent information to add
     * @return component detail of updated text component
     */
    @PermitAll
    @PutMapping("")
    public ResponseEntity<ComponentDetailDto> updateTextComponent(
        @RequestBody TextUpdateDto textComponent) {
        return new ResponseEntity<>(service.updateTextComponent(textComponent), HttpStatus.CREATED);
    }
}
