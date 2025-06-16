package at.ac.tuwien.sepr.groupphase.backend.endpoint.groupendpoints;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDetailWithMembersDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.group.GroupMemberDto;
import at.ac.tuwien.sepr.groupphase.backend.service.groupservice.GroupService;
import jakarta.annotation.security.PermitAll;
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

import java.util.Set;

/**
 * REST controller for managing group operations.
 * This controller provides endpoints for creating, updating, retrieving, and deleting groups.
 * Specifically, it includes operations for managing groups within the application.
 */
@RestController
@RequestMapping(value = "/api/v1/group")
public class GroupEndpoint {

    private final GroupService service;

    public GroupEndpoint(final GroupService groupService) {
        this.service = groupService;
    }

    /**
     * Creates a group.
     *
     * @return created group
     */
    @PermitAll // TODO: fix this
    @PostMapping
    public ResponseEntity<GroupDetailDto> createGroup() {
        return new ResponseEntity<>(service.createGroup(), HttpStatus.CREATED);
    }

    /**
     * Updates a groups name.
     *
     * @param dto group to be updated
     * @return updated group
     */
    @PermitAll // TODO: fix this
    @PutMapping("name")
    public ResponseEntity<GroupDetailDto> updateGroupName(@RequestBody GroupDto dto) {
        return new ResponseEntity<>(service.updateGroupName(dto), HttpStatus.OK);
    }

    /**
     * Updates a groups owner.
     *
     * @param dto group to be updated
     * @return updated group
     */
    @PermitAll // TODO: fix this
    @PutMapping("owner")
    public ResponseEntity<GroupDetailDto> updateGroupOwner(@RequestBody GroupMemberDto dto) {
        return new ResponseEntity<>(service.updateGroupOwner(dto), HttpStatus.OK);
    }

    /**
     * Gets a group by id with all members.
     *
     * @param id of the group
     * @return group with all members
     */
    @PermitAll // TODO: fix this
    @GetMapping("{id}")
    public ResponseEntity<GroupDetailWithMembersDto> getGroup(@PathVariable("id") long id) {
        return new ResponseEntity<>(service.getGroup(id), HttpStatus.OK);
    }

    /**
     * Gets all groups where the user is part of.
     *
     * @return all groups where user is part of
     */
    @GetMapping
    @PermitAll // TODO: fix this
    public ResponseEntity<Set<GroupDetailDto>> getAllGroups() {
        return new ResponseEntity<>(service.getAllGroups(), HttpStatus.OK);
    }

    /**
     * Gets all groups where the user is owner of.
     *
     * @return all groups where user is owner of
     */
    @PermitAll // TODO: fix this
    @GetMapping("/my")
    public ResponseEntity<Set<GroupDto>> getMyGroups() {
        return new ResponseEntity<>(service.getMyGroups(), HttpStatus.OK);
    }

    /**
     * Gets all groups where this user is owner and given user is member.
     *
     * @return all groups where this user is owner of and given user is member
     */
    @PermitAll // TODO: fix this
    @GetMapping("/my/{username}")
    public ResponseEntity<Set<GroupDto>> getCommonGroups(@PathVariable("username") String username) {
        return new ResponseEntity<>(service.getCommonGroups(username), HttpStatus.OK);
    }

    /**
     * Deletes a group.
     *
     * @param id of group to delete
     * @return void
     */
    @PermitAll // TODO: fix this
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") long id) {
        service.deleteGroup(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
