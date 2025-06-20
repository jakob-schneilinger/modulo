package at.ac.tuwien.sepr.groupphase.backend.endpoint.groupendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupBoardDto;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupBoardService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/group/board")
public class GroupBoardEndpoint {

    private final GroupBoardService service;

    GroupBoardEndpoint(GroupBoardService service) {
        this.service = service;
    }

    /**
     * Adds group tp board.
     *
     * @param dto info
     * @return void
     */
    @PostMapping()
    public ResponseEntity<Void> addGroupToBoard(@RequestBody GroupBoardDto dto) {
        service.addGroupToBoard(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Removes Group from board.
     *
     * @param dto info
     * @return void
     */
    @DeleteMapping()
    public ResponseEntity<Void> removeGroupFromBoard(@RequestBody GroupBoardDto dto) {
        service.removeGroupFromBoard(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Updates permission for one group with one board.
     *
     * @param dto info
     * @return void
     */
    @PutMapping()
    public ResponseEntity<Void> updateBoardPermission(@RequestBody GroupBoardDto dto) {
        service.updateBoardPermission(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets all shared root boards.
     *
     * @return All shared root boards with group infos
     */
    @GetMapping()
    public ResponseEntity<List<ComponentDetailDto>> getGroupRoots() {
        return new ResponseEntity<>(service.getGroupRoots(), HttpStatus.OK);
    }

    /**
     * Get list of all groups where given board is shared to.
     *
     * @param id of the board
     * @return List of groups
     */
    @GetMapping("{id}")
    public ResponseEntity<List<GroupBoardDto>> getGroupsByBoardId(@PathVariable("id") long id) {
        return new ResponseEntity<>(service.getGroupsByBoardId(id), HttpStatus.OK);
    }

    /**
     * Checks if user has write permissions on board with given id.
     *
     * @param id of the board
     * @return null if owner, true if write permission, else false
     */
    @GetMapping("permission/{id}")
    public ResponseEntity<Boolean> hasWritePermission(@PathVariable("id") long id) {
        return new ResponseEntity<>(service.hasWritePermission(id), HttpStatus.OK);
    }
}
