package com.hdtpt.pentachat.identity.service;

import com.hdtpt.pentachat.identity.dto.request.UpdateProfileRequest;
import com.hdtpt.pentachat.identity.dto.response.ProfileResponse;
import com.hdtpt.pentachat.identity.repository.ProfileRepository;
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
@DisplayName("Profile Service Tests")
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private ProfileRepository profileRepository;

    @BeforeEach
    void setUp() {
        profileRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create and retrieve profile")
    void testCreateAndGetProfile() {
        Long userId = 100L;
        profileService.createProfile(userId);

        ProfileResponse response = profileService.getProfile(userId);
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
    }

    @Test
    @DisplayName("Should update profile fields")
    void testUpdateProfile() {
        Long userId = 200L;
        profileService.createProfile(userId);

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Test User");
        request.setBio("Hello world");

        ProfileResponse updated = profileService.updateProfile(userId, request);
        assertEquals("Test User", updated.getFullName());
        assertEquals("Hello world", updated.getBio());
    }
}
