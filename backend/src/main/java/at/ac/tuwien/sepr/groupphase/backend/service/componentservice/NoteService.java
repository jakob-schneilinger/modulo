package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.NoteUpdateDto;

public interface NoteService {

    /**
     * Creates a note in the database.
     *
     * @param note to create
     * @return Component detail of the note created with all subcomponents
     */
    ComponentDetailDto createNote(NoteCreateDto note);

    /**
     * Updates a note in the database.
     *
     * @param note to update
     * @return Component detail of the note updated with all subcomponents
     */
    ComponentDetailDto updateNote(NoteUpdateDto note);
}
