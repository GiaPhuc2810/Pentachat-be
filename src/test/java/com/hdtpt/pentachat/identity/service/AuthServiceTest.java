package com.hdtpt.pentachat.identity.service;

import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Authentication Service Tests")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a new user successfully")
    void testRegister_Success() {
        User user = authService.register("new_user", "password123");
        assertNotNull(user);
        assertEquals("new_user", user.getUsername());
        assertTrue(userRepository.existsByUsername("new_user"));
    }

    @Test
    @DisplayName("Should throw error when registering duplicate username")
    void testRegister_DuplicateUsername() {
        authService.register("user_exists", "password123");

        AppException exception = assertThrows(AppException.class, () -> {
            authService.register("user_exists", "another_password");
        });

        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Should login successfully with correct credentials")
    void testLogin_Success() {
        authService.register("test_user_login", "correct_password");

        User user = authService.login("test_user_login", "correct_password");
        assertNotNull(user);
        assertEquals("test_user_login", user.getUsername());
    }

    @Test
    @DisplayName("Should throw error with wrong password")
    void testLogin_WrongPassword() {
        authService.register("test_user_password", "correct_password");

        AppException exception = assertThrows(AppException.class, () -> {
            authService.login("test_user_password", "wrong_password");
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    @DisplayName("Should create session successfully")
    void testCreateSession() {
        User user = authService.register("session_user", "password");
        String sessionId = authService.createSession(user);
        assertNotNull(sessionId);
        assertFalse(sessionId.isEmpty());
    }
}
