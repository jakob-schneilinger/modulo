package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;

public interface UserService {

    /**
     * Updates an user.
     *
     * @param username the user to update
     * @param userUpdateDto the values to update, if null they won't be updated
     */
    void update(String username, UserUpdateDto userUpdateDto);

    /**
     * Gets an user.
     *
     * @param username the specified user
     */
    UserDto get(String username);

    /**
     * Deletes an user.
     *
     * @param username the user to delete
     */
    void delete(String username);

    /**
     * Save avatar.
     *
     * @param username the user to save the avatar to
     */
    void saveAvatar(String username, byte[] image);

    /**
     * Removes an avatar.
     *
     * @param username the user to delete the avatar
     */
    void removeAvatar(String username);
}
