package at.ac.tuwien.sepr.groupphase.backend.endpoint.groupendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupMemberService;
import jakarta.annotation.security.PermitAll;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/group/member")
public class GroupMemberEndpoint {

    private final GroupMemberService service;

    public GroupMemberEndpoint(GroupMemberService groupMemberService) {
        this.service = groupMemberService;
    }

    /**
     * Adds a member to a group.
     *
     * @param dto member and group info
     * @return void
     */
    @PermitAll //TODO: fix this
    @PostMapping()
    public ResponseEntity<Void> addGroupMember(@RequestBody GroupMemberDto dto) {
        service.addGroupMember(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Deletes a member from a group.
     *
     * @param dto member and group info
     * @return void
     */
    @PermitAll //TODO: fix this
    @DeleteMapping()
    public ResponseEntity<Void> deleteGroupMember(@RequestBody GroupMemberDto dto) {
        service.deleteGroupMember(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Leaves a group.
     *
     * @param id of group to leave
     * @return void
     */
    @PermitAll // TODO: fix this
    @PostMapping("leave/{id}")
    public ResponseEntity<Void> leaveGroup(@PathVariable("id") long id) {
        service.leaveGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
