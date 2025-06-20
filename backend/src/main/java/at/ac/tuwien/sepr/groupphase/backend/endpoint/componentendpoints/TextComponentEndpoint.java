package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.TextService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing text component operations.
 * This controller provides endpoints for creating and updating text components.
 */
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
    @PatchMapping("")
    public ResponseEntity<ComponentDetailDto> updateTextComponent(
        @RequestBody TextUpdateDto textComponent) {
        return new ResponseEntity<>(service.updateTextComponent(textComponent), HttpStatus.CREATED);
    }
}
