package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.FriendDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendServiceImpl implements FriendService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;


    @Autowired
    public FriendServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String currentUserName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void validateUser(String username, String authName) {
        if (!authName.equals(username)) {
            throw new ForbiddenException("You don't have permissions to manage this user");
        }
    }

    private ApplicationUser userExists(String username) throws NotFoundException {
        Optional<ApplicationUser> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new NotFoundException("User " + username + " does not exist");
        }
        return user.get();
    }

    @Override
    @Transactional
    public void requestFriendship(String myUsername, String friendUsername) {
        String myAuthUsername = currentUserName();
        LOGGER.trace("friendship request for {} from {}", friendUsername, myUsername);

        // make sure both users exist
        validateUser(myUsername, myAuthUsername);
        ApplicationUser me = userExists(myUsername);
        ApplicationUser friend = userExists(friendUsername);

        // check if the request already exists
        if (userRepository.requestExists(me.getId(), friend.getId())) {
            throw new ConflictException("There is already a friend request between given users", new ArrayList<>());
        }

        userRepository.createFriendRequest(me.getId(), friend.getId());
    }

    @Override
    @Transactional
    public void acceptFriendship(String myUsername, String friendUsername) {
        String myAuthUsername = currentUserName();
        LOGGER.trace("friendship accept request for {} from {}", friendUsername, myUsername);

        // make sure both users exist
        validateUser(myUsername, myAuthUsername);
        ApplicationUser me = userExists(myUsername);
        ApplicationUser friend = userExists(friendUsername);

        userRepository.acceptFriendRequest(me.getId(), friend.getId());
    }

    @Override
    @Transactional
    public void deleteFriendship(String myUsername, String friendUsername) {
        String myAuthUsername = currentUserName();
        LOGGER.trace("friendship delete for {} from {}", friendUsername, myUsername);

        // make sure both users exist
        validateUser(myUsername, myAuthUsername);
        ApplicationUser me = userExists(myUsername);
        ApplicationUser friend = userExists(friendUsername);

        userRepository.deleteFriend(me.getId(), friend.getId());
    }

    @Override
    public List<FriendDto> getAllFriends(String myUsername, boolean onlyFriends) {
        String myAuthUsername = currentUserName();
        LOGGER.trace("get all friends of {}", myUsername);

        // make sure both users exist
        validateUser(myUsername, myAuthUsername);
        ApplicationUser me = userExists(myUsername);

        return userRepository.getAllFriends(me.getId(), onlyFriends);
    }

    @Override
    public boolean isFriend(String myUsername, String friendUsername) {
        String myAuthUsername = currentUserName();
        LOGGER.trace("is {} friends with {}", myUsername, friendUsername);

        // make sure both users exist
        validateUser(myUsername, myAuthUsername);
        ApplicationUser me = userExists(myUsername);
        ApplicationUser friend = userExists(friendUsername);

        return this.userRepository.isFriend(me.getId(), friend.getId());
    }

    @Override
    public FriendDto getFriend(String myUsername, String friendUsername) {
        String myAuthUsername = currentUserName();
        LOGGER.trace("is {} friends with {}", myUsername, friendUsername);

        // make sure both users exist
        validateUser(myUsername, myAuthUsername);
        ApplicationUser me = userExists(myUsername);
        ApplicationUser friend = userExists(friendUsername);

        if (! this.userRepository.requestExists(me.getId(), friend.getId())) {
            throw new NotFoundException("No friend info name " + friendUsername + " was not found");
        }

        boolean isFriend = this.userRepository.isFriend(me.getId(), friend.getId());
        boolean mineRequest = this.userRepository.iRequested(me.getId(), friend.getId());
        String requesterName = mineRequest ? me.getUsername() : friend.getUsername();

        return new FriendDto(friend.getUsername(), friend.getDisplayName(), friend.getEmail(), requesterName, isFriend);
    }
}
