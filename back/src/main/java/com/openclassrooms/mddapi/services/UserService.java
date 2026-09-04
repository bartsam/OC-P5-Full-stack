package com.openclassrooms.mddapi.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.openclassrooms.mddapi.dto.UserUpdateRequest;
import com.openclassrooms.mddapi.exceptions.UserAlreadyExistsException;
import com.openclassrooms.mddapi.models.UserEntity;
import com.openclassrooms.mddapi.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Constructs the UserService with required dependencies.
     *
     * @param userRepository  the repository for managing UserEntity persistence
     * @param passwordEncoder the encoder used for hashing passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a user by their stable database identifier.
     *
     * @param userId Current id of the user
     * @return the matched UserEntity
     * @throws EntityNotFoundException if no user is found
     */
    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    /**
     * Updates an existing user profile information (email, username, password).
     *
     * @param userId  Current id of the user to update
     * @param request the payload with new email, username, and raw password
     * @return the updated and saved UserEntity
     * @throws EntityNotFoundException    if user to update is not found
     * @throws UserAlreadyExistsException if email or username already used
     */
    public UserEntity updateUser(Long userId, UserUpdateRequest request) {
        UserEntity user = findById(userId);

        if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new UserAlreadyExistsException("Email is already in use");
        }
        if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
            throw new UserAlreadyExistsException("Username is already in use");
        }

        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        return userRepository.save(user);
    }

}
