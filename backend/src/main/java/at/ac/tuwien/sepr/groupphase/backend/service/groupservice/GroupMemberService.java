package at.ac.tuwien.sepr.groupphase.backend.service.groupservice;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;

public interface GroupMemberService {

    /**
     * Adds a member to a group.
     *
     * @param dto member and group info
     */
    void addGroupMember(GroupMemberDto dto);

    /**
     * Deletes a member from a group.
     *
     * @param dto member and group info
     */
    void deleteGroupMember(GroupMemberDto dto);

    /**
     * Leaves a group.
     *
     * @param id of group to leave
     */
    void leaveGroup(long id);
}
