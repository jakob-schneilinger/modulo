package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.NoteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing note component operations.
 * This controller provides endpoints for creating and updating note components.
 */
@RestController
@RequestMapping(value = "/api/v1/component/note")
public class NoteComponentEndpoint {

    private final NoteService service;

    @Autowired
    public NoteComponentEndpoint(NoteService noteService) {
        this.service = noteService;
    }

    /**
     * Creates a note in the database.
     *
     * @param note information to add
     * @return Component detail of created note
     */
    @PostMapping("")
    public ResponseEntity<ComponentDetailDto> createBoard(
        @RequestBody NoteCreateDto note) {
        return new ResponseEntity<>(service.createNote(note), HttpStatus.CREATED);
    }

    /**
     * Updates a note in the database.
     *
     * @param note information to update
     * @return Component detail of updated note
     */
    @PatchMapping("")
    public ResponseEntity<ComponentDetailDto> updateBoard(
        @RequestBody NoteUpdateDto note) {
        return new ResponseEntity<>(service.updateNote(note), HttpStatus.OK);
    }
}
