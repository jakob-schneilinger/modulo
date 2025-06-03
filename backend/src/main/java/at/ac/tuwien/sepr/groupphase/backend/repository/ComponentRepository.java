package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.components.Component;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<Component, Long> {

    @Query(nativeQuery = true, value = """
            SELECT c.id
            FROM components c
            WHERE c.owner_id = :ownerId
              AND c.id NOT IN (
                  SELECT cc.id_child
                  FROM container_children cc
              )
            """)
    List<Long> getComponentIdsByOwnerId(@Param("ownerId") long ownerId);

    @Modifying
    @Query(value = "INSERT INTO container_children (id_container, id_child) VALUES (:parentId, :childId)", nativeQuery = true)
    void link(@Param("parentId") Long parentId, @Param("childId") Long childId);

    @Modifying
    @Query(value = "DELETE FROM container_children WHERE id_child = :childId", nativeQuery = true)
    void unlink(@Param("childId") Long childId);

    @Query(value = """
            WITH RECURSIVE parent_tree(id, depth) AS (
              SELECT id_child, 0 FROM container_children WHERE id_child = :childId
              UNION ALL
              SELECT cc.id_container, pt.depth + 1
              FROM container_children cc
              JOIN parent_tree pt ON cc.id_child = pt.id
            )
            SELECT COALESCE(MAX(depth), 0) FROM parent_tree
            """, nativeQuery = true)
    Integer getParentDepth(@Param("childId") Long childId);

    @Query(value = """
            SELECT id_container FROM container_children
            WHERE id_child = :childId
            """, nativeQuery = true)
    Long getParentId(@Param("childId") Long childId);

    Optional<Component> findByChildren_Id(Long childId);
}