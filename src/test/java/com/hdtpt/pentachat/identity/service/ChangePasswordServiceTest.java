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
@DisplayName("Change Password Service Tests")
class ChangePasswordServiceTest {

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
    @DisplayName("Should change password successfully")
    void testChangePassword_Success() {
        User user = authService.register("change_pass_user", "old_pass");

        User updated = authService.changePassword(user.getId(), "old_pass", "new_pass");

        assertNotNull(updated);
        assertEquals("new_pass", updated.getPassword());
        assertThrows(AppException.class, () -> authService.login("change_pass_user", "old_pass"));
        User loggedIn = authService.login("change_pass_user", "new_pass");
        assertEquals(user.getId(), loggedIn.getId());
    }

    @Test
    @DisplayName("Should throw error when current password is incorrect")
    void testChangePassword_WrongCurrentPassword() {
        User user = authService.register("wrong_current_user", "old_pass");

        AppException exception = assertThrows(AppException.class, () -> {
            authService.changePassword(user.getId(), "bad_pass", "new_pass");
        });

        assertEquals("Invalid current password", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw error when new password matches current password")
    void testChangePassword_SamePassword() {
        User user = authService.register("same_pass_user", "same_pass");

        AppException exception = assertThrows(AppException.class, () -> {
            authService.changePassword(user.getId(), "same_pass", "same_pass");
        });

        assertEquals("New password must be different from current password", exception.getMessage());
    }
}
