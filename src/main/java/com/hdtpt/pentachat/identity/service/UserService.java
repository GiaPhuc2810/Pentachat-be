package com.hdtpt.pentachat.identity.service;

import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Search users by username pattern (case-insensitive)
     * 
     * @param query Search query
     * @return List of matching users
     */
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCase(query);
    }

    /**
     * Create a new user with username and password
     * 
     * @param username User's username
     * @param password User's password
     * @return Created user entity
     */
    public User createUser(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(password)
                .build();

        return userRepository.save(user);
    }

    /**
     * Find user by username
     * 
     * @param username Username to search for
     * @return User entity
     * @throws RuntimeException if user not found
     */
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Find user by ID
     * 
     * @param userId User ID to search for
     * @return User entity
     * @throws RuntimeException if user not found
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User ID not found: " + userId));
    }

    /**
     * Check if user exists by username
     * 
     * @param username Username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}
