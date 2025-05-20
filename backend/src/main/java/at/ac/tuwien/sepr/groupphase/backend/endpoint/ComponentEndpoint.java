package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.ComponentService;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * REST controller for managing component operations.
 * This controller provides endpoints for creating, updating, retrieving, and deleting components.
 * Specifically, it includes operations for managing boards within the application.
 */
@RestController
@RequestMapping(value = "/api/v1/component")
public class ComponentEndpoint {

    private static final Logger log = LoggerFactory.getLogger(ComponentEndpoint.class);
    private final ComponentService service;

    @Autowired
    public ComponentEndpoint(ComponentService componentService) {
        this.service = componentService;
    }


    /**
     * Creates a TextComponent in the database.
     *
     * @param textComponent information to add
     * @return Component detail of created TextComponent
     */
    @PermitAll
    @PostMapping("/text")
    public ResponseEntity<ComponentDetailDto> createTextComponent(
        @RequestHeader(name = "Authorization") String token,
        @RequestBody TextCreateDto textComponent) {
        return new ResponseEntity<>(service.createTextComponent(textComponent), HttpStatus.CREATED);
    }

    /**
     * Updates a TextComponent in the database.
     *
     * @param textComponent information to add
     * @return Component detail of updated TextComponent
     */
    @PermitAll
    @PutMapping("/text")
    public ResponseEntity<ComponentDetailDto> updateTextComponent(
        @RequestHeader(name = "Authorization") String token,
        @RequestBody TextUpdateDto textComponent) {
        return new ResponseEntity<>(service.updateTextComponent(textComponent), HttpStatus.CREATED);
    }

    /**
     * Creates a board in the database.
     *
     * @param board information to add
     * @return Component detail of created board
     */
    @PermitAll  // TODO: fix this
    @PostMapping("/board")
    public ResponseEntity<ComponentDetailDto> createBoard(
        @RequestBody BoardCreateDto board) {
        return new ResponseEntity<>(service.createBoard(board), HttpStatus.CREATED);
    }

    /**
     * Updates a board in the database.
     *
     * @param board information to update
     * @return Component detail of updated board
     */
    @PermitAll  // TODO: fix this
    @PutMapping("/board")
    public ResponseEntity<ComponentDetailDto> updateBoard(
        @RequestBody BoardUpdateDto board) {
        return new ResponseEntity<>(service.updateBoard(board), HttpStatus.OK);
    }

    /**
     * Gets the details of the component with given id including all children.
     *
     * @return Component detail of component with all subcomponents
     */
    @PermitAll  // TODO: fix this
    @GetMapping("{id}")
    public ResponseEntity<ComponentDetailDto> getComponent(@PathVariable("id") long id) {
        return new ResponseEntity<>(service.getComponentById(id), HttpStatus.OK);
    }

    /**
     * Gets a list of all root components (components without a parent) NOT including children.
     *
     * @return List of the details of the root components
     */
    @PermitAll  // TODO: fix this
    @GetMapping()
    public ResponseEntity<List<ComponentDetailDto>> getRootComponents() {
        return new ResponseEntity<>(service.getRootComponents(), HttpStatus.OK);
    }

    /**
     * Deletes a component.
     *
     * @param id of component to delete
     * @return void
     */
    @PermitAll  // TODO: fix this
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteComponent(
        @PathVariable("id") long id) {
        service.deleteComponent(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}