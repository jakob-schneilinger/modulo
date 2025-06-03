package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components;

import java.util.List;

/**
 * The NoteDto interface extends ContainerDto, representing
 * Data Transfer Objects (DTOs) for note components.
 */
public interface NoteDto extends ContainerDto {

    /**
     * Title of the note.
     *
     * @return title
     */
    String title();

    /**
     * List of labels associated with the note.
     *
     * @return List of labels
     */
    List<LabelDto> labels();
}