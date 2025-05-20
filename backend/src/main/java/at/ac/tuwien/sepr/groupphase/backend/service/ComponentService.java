package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.TextUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;

import java.util.List;

public interface ComponentService {

    /**
     * Creates a TextComponent in database.
     *
     * @param textCreateDto board to be created
     * @return Component detail of the TextComponent created
     */

    ComponentDetailDto createTextComponent(TextCreateDto textCreateDto);

    /**
     * Creates a Board in database.
     *
     * @param board board to be created
     * @return Component detail of the board created
     */
    ComponentDetailDto createBoard(BoardCreateDto board);

    /**
     * Updates a Board in database.
     *
     * @param board board to be updated
     * @return Component detail of the board updated with all subcomponents
     */
    ComponentDetailDto updateBoard(BoardUpdateDto board);

    /**
     * Gets the details of the component with given id including all children.
     *
     * @param id id of the component to get
     * @return Component detail of the component with all subcomponents
     */
    ComponentDetailDto getComponentById(long id);

    /**
     * Gets all details of the root components of a user (all components without a parent).
     *
     * @return List of component details of the root components
     */
    List<ComponentDetailDto> getRootComponents();

    /**
     * Deletes a component from the database.
     *
     * @param id of component to delete
     */
    void deleteComponent(Long id);

    /**
     * Updates a Text component in database.
     *
     * @param textComponent to be updated
     * @return Component detail of the text component updated
     */
    ComponentDetailDto updateTextComponent(TextUpdateDto textComponent);

}
