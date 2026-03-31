package com.hdtpt.pentachat.identity.service;

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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("User Service Tests")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindUserByUsername() {
        userService.createUser("find_me", "pass");
        User user = userService.findUserByUsername("find_me");
        assertNotNull(user);
        assertEquals("find_me", user.getUsername());
    }

    @Test
    @DisplayName("Should search users by username pattern")
    void testSearchUsers() {
        userService.createUser("alpha_user", "pass");
        userService.createUser("beta_user", "pass");
        userService.createUser("gamma_user", "pass");

        List<User> results = userService.searchUsers("user");
        assertEquals(3, results.size());

        results = userService.searchUsers("alpha");
        assertEquals(1, results.size());
        assertEquals("alpha_user", results.get(0).getUsername());
    }

    @Test
    @DisplayName("Should check if user exists")
    void testUserExists() {
        assertFalse(userService.userExists("exists_check"));
        userService.createUser("exists_check", "pass");
        assertTrue(userService.userExists("exists_check"));
    }
}
