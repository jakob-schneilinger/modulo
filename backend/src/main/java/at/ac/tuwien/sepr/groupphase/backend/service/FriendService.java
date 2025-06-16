package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.FriendDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

import java.util.List;

public interface FriendService {

    /**
     * creates a friendship request with myUsername as requester.
     *
     * @param myUsername username of user that is requesting
     * @param friendUsername username of user that is being requested
     *
     * @throws ForbiddenException - in case authentication problem
     * @throws NotFoundException - in case given user(s) do not exist
     * @throws ConflictException - if request already exists or requesting self
     */
    void requestFriendship(String myUsername, String friendUsername)
        throws ForbiddenException, NotFoundException, ConflictException;

    /**
     * accepts a friendship request with myUsername as requester
     * and friendUsername as accepter.
     *
     * @param myUsername username of user that is being requested
     *                   to accept a friendship
     * @param friendUsername username of user that is requesting
     *
     * @throws ForbiddenException - in case authentication problem
     * @throws NotFoundException - in case given user(s) do not exist
     * @throws ConflictException - in case there is no request to accept or accepting self
     */
    void acceptFriendship(String myUsername, String friendUsername)
        throws ForbiddenException, NotFoundException;


    /**
     * deletes a friendship of myUsername and friendUsername
     * in any status(requested/accepted).
     *
     * @param myUsername username of user that is requesting
     * @param friendUsername username of user
     *                       whose friendship with user is to be deleted
     *
     * @throws ForbiddenException - in case authentication problem
     * @throws NotFoundException - in case given user(s) do not exist
     */
    void deleteFriendship(String myUsername, String friendUsername)
        throws ForbiddenException, NotFoundException;


    /**
     * gets all friends, requests from and to user with username.
     *
     * @param myUsername username of user that is requesting
     * @param onlyFriends if true, get only accepted friendships
     *
     * @throws ForbiddenException - in case authentication problem
     * @throws NotFoundException - in case given user(s) do not exist
     */
    List<FriendDto> getAllFriends(String myUsername, boolean onlyFriends)
        throws ForbiddenException, NotFoundException;


    /**
     * check whether user with friendUsername username is an accepter friend
     * of the user with myUsername username.
     *
     * @param myUsername username of user that is requesting
     * @param friendUsername username of user
     *                       whose friendship with user is tested
     * @return true if user with friendUsername is an accepted friend of user with myUsername
     *
     * @throws ForbiddenException - in case authentication problem
     * @throws NotFoundException - in case given user(s) do not exist
     */
    boolean isFriend(String myUsername, String friendUsername)
        throws ForbiddenException, NotFoundException;

    /**
     * gets the friend, or friend request from and to user with username.
     *
     * @param myUsername username of user that is requesting
     * @param friendUsername username of user
     *                   who is friend with user
     *                   or a friendship request was sent
     *
     * @throws ForbiddenException - in case authentication problem
     * @throws NotFoundException - in case given user(s) do not exist
     */
    FriendDto getFriend(String myUsername, String friendUsername);
}
