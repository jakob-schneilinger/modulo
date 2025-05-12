package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.user.UserUpdateDto;

public interface UserService {

    /**
     * Updates an user.
     *
     * @param userUpdateDto the values to update, if null they won't be updated
     */
    void update(UserUpdateDto userUpdateDto);

    /**
     * Deletes an user.
     *
     * @param username the user to delete
     */
    void delete(String username, String token);
}
