package at.ac.tuwien.sepr.groupphase.backend.service.groupservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailWithMembersDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;

import java.util.Set;

public interface GroupService {

    /**
     * Creates a group.
     *
     * @return created group
     */
    GroupDetailDto createGroup();

    /**
     * Updates a groups name.
     *
     * @param dto group to update
     * @return updated group
     */
    GroupDetailDto updateGroupName(GroupDto dto);

    /**
     * Updates a groups owner.
     *
     * @param dto group to update
     * @return updated group
     */
    GroupDetailDto updateGroupOwner(GroupMemberDto dto);

    /**
     * Gets a group by id with all members.
     *
     * @param id of the group
     * @return group with all members
     */
    GroupDetailWithMembersDto getGroup(long id);

    /**
     * Gets all groups where the user ist part of.
     *
     * @return all groups where user is part of
     */
    Set<GroupDetailDto> getAllGroups();

    /**
     * Gets all groups where the user is owner of.
     *
     * @return all groups where user is owner of
     */
    Set<GroupDto> getMyGroups();

    /**
     * Gets all groups where this user is owner and given user is member.
     *
     * @param username of the given user
     * @return all groups where this user is owner of and given user is member
     */
    Set<GroupDto> getCommonGroups(String username);

    /**
     * Deletes the group with given id.
     *
     * @param id of the group to delete
     */
    void deleteGroup(Long id);
}
