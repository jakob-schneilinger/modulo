package at.ac.tuwien.sepr.groupphase.backend.service.componentservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;

public interface BoardService {

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
}
