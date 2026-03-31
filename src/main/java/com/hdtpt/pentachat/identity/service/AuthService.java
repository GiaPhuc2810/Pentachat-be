package com.hdtpt.pentachat.identity.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hdtpt.pentachat.identity.repository.UserRepository;
import com.hdtpt.pentachat.finance.service.WalletService;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.security.SessionManager;
import com.hdtpt.pentachat.identity.model.User;

/**
 * Authentication service
 * Handles user registration and login
 * 
 * Business logic layer - does NOT depend directly on mock data
 * Depends on DataApi interface for data access
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
            ProfileService profileService,
            WalletService walletService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     * 
     * @param username user's username
     * @param password user's password
     * @return registered user
     * @throws AppException if username already exists
     */
    public User register(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AppException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AppException("Password cannot be empty");
        }

        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new AppException("Username already exists");
        }

        // Create user
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        newUser = userRepository.save(newUser);

        // Create wallet and profile for new user
        profileService.createProfile(newUser.getId());
        walletService.createWallet(newUser.getId(), 0.0);

        return newUser;
    }

    /**
     * Login user
     * 
     * @param username user's username
     * @param password user's password
     * @return user if credentials are correct
     * @throws AppException if user not found or password is incorrect
     */
    public User login(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AppException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AppException("Password cannot be empty");
        }

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException("Invalid username or password");
        }

        return user;
    }

    /**
     * Create session for user
     * 
     * @param user logged-in user
     * @return session ID
     */
    public String createSession(User user) {
        return SessionManager.createSession(user.getId(), user.getUsername());
    }

    /**
     * Change user's password
     * 
     * @param userId          user's id
     * @param currentPassword current password
     * @param newPassword     new password
     * @return updated user
     * @throws AppException if user not found or password invalid
     */
    public User changePassword(Long userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new AppException("User ID is required");
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new AppException("Current password cannot be empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new AppException("New password cannot be empty");
        }
        if (currentPassword.equals(newPassword)) {
            throw new AppException("New password must be different from current password");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AppException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User resetPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new AppException("User ID is required");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new AppException("New password cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
