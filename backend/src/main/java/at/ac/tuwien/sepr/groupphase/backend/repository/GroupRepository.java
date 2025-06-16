package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Finds all groups with given user as owner.
     *
     * @param owner of the groups
     * @return all groups where given user is owner
     */
    Set<Group> findByOwner(ApplicationUser owner);

    /**
     * Finds all groups with given user as member.
     *
     * @param member of the groups
     * @return all groups where given user is member of
     */
    Set<Group> findByMembersContaining(ApplicationUser member);

    /**
     * Validates if given user is member of given group and that given group exists.
     *
     * @param groupId id of given group
     * @param member user
     * @return true if user is member of given group and group exists
     */
    boolean existsByIdAndMembersContaining(Long groupId, ApplicationUser member);
}
