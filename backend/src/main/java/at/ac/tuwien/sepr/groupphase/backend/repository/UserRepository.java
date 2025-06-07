package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.FriendDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, String> {
    Optional<ApplicationUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query(nativeQuery = true, value = "insert into friends values (:myId, :friendId, false)")
    void createFriendRequest(@Param("myId") Long myId, @Param("friendId") Long friendId);

    @Modifying
    @Query(nativeQuery = true, value = """
        update friends
        set accepted = true
        where (requester_id = :friendId and accepter_id = :myId)
        """)
    void acceptFriendRequest(@Param("myId") Long myId, @Param("friendId") Long friendId);

    @Modifying
    @Query(nativeQuery = true, value = """
        delete from friends
        where (requester_id = :myId and accepter_id = :friendId) or (requester_id = :friendId and accepter_id = :myId)""")
    void deleteFriend(@Param("myId") Long myId, @Param("friendId") Long friendId);

    @Query(nativeQuery = true, value = """
        select u.username, u.display_name, u.email,
        (select username from users where id = :myId limit 1), f.accepted
        from users u
        join friends f on u.id = f.accepter_id
        where f.requester_id = :myId and f.accepted >= :accepted
        union
        select u.username, u.display_name, u.email,
         u.username, f.accepted
        from users u
        join friends f on u.id = f.requester_id
        where f.accepter_id = :myId and f.accepted >= :accepted
        """)
    List<FriendDto> getAllFriends(@Param("myId") Long myId, @Param("accepted") boolean onlyFriends);

    @Query(nativeQuery = true, value = """
        select coalesce(
        (select accepted from friends
            where (requester_id = :myId and accepter_id = :friendId) or (requester_id = :friendId and accepter_id = :myId)
        ),
        false)
        """)
    boolean isFriend(@Param("myId") Long myId, @Param("friendId") Long friendId);

    @Query(nativeQuery = true, value = """
        select
        (select accepted from friends
        where (requester_id = :myId and accepter_id = :friendId) or (requester_id = :friendId and accepter_id = :myId))
        is not null
        """)
    boolean requestExists(@Param("myId") Long myId, @Param("friendId") Long friendId);

    @Query(nativeQuery = true, value = """
        select casewhen((select requester_id from friends
                         where requester_id = :myId and accepter_id = :friendId) is not null,
                        true, false)
        """)
    boolean iRequested(@Param("myId") Long myId, @Param("friendId") Long friendId);
}
