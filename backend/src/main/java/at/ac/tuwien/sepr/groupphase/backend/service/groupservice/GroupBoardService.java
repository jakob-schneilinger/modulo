package at.ac.tuwien.sepr.groupphase.backend.service.groupservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.components.ComponentDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupBoardDto;

import java.util.List;
import java.util.Map;

public interface GroupBoardService {

    /**
     * Adds Group to Board.
     *
     * @param groupBoardDto info
     */
    void addGroupToBoard(GroupBoardDto groupBoardDto);

    /**
     * Removes Group from board.
     *
     * @param groupBoardDto info
     */
    void removeGroupFromBoard(GroupBoardDto groupBoardDto);

    /**
     * updates Permission of one group with one board.
     *
     * @param groupBoardDto info
     */
    void updateBoardPermission(GroupBoardDto groupBoardDto);

    /**
     * Gets all shared root boards with group info.
     *
     * @return all shared root boards
     */
    List<ComponentDetailDto> getGroupRoots();

    /**
     * Get list of all groups where given board is shared to.
     *
     * @param id of the board
     * @return List of groups
     */
    List<GroupBoardDto> getGroupsByBoardId(long id);

    /**
     * Checks if user has write permissions on board with given id.
     *
     * @param id of the board
     * @return null if owner, true if write permission, else false
     */
    Boolean hasWritePermission(long id);
}
