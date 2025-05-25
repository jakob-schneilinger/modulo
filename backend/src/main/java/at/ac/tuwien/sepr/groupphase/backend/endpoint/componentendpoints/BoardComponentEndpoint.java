package at.ac.tuwien.sepr.groupphase.backend.endpoint.componentendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.BoardUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.service.componentservice.BoardService;
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
@RequestMapping(value = "/api/v1/component/board")
public class BoardComponentEndpoint {

    private final BoardService service;

    @Autowired
    public BoardComponentEndpoint(BoardService boardService) {
        this.service = boardService;
    }

    /**
     * Creates a board in the database.
     *
     * @param board information to add
     * @return component detail of created board
     */
    @PermitAll // TODO: fix this
    @PostMapping("")
    public ResponseEntity<ComponentDetailDto> createBoardComponent(
            @RequestBody BoardCreateDto board) {

        return new ResponseEntity<>(service.createBoard(board), HttpStatus.CREATED);
    }

    /**
     * Updates a board in the database.
     *
     * @param board information to update
     * @return component detail of updated board
     */
    @PermitAll // TODO: fix this
    @PutMapping("")
    public ResponseEntity<ComponentDetailDto> updateBoardComponent(
            @RequestBody BoardUpdateDto board) {

        return new ResponseEntity<>(service.updateBoard(board), HttpStatus.OK);
    }
}
