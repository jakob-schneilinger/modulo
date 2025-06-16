package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.group.Group;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.Permission;
import at.ac.tuwien.sepr.groupphase.backend.entity.group.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, PermissionId> {

    /**
     * Finds all permissions for a given component.
     *
     * @param id where to get all permissions from
     * @return a set of all permissions for this component
     */
    Set<Permission> findByComponent_Id(long id);

    /**
     * Removes all permission of a group.
     *
     * @param group where to remove all permissions from
     */
    void removeAllByGroup(Group group);
}
