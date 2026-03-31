package com.hdtpt.pentachat.identity.controller;

import org.springframework.web.bind.annotation.*;
import com.hdtpt.pentachat.identity.service.ProfileService;
import com.hdtpt.pentachat.identity.dto.request.UpdateProfileRequest;
import com.hdtpt.pentachat.identity.dto.response.ProfileResponse;
import com.hdtpt.pentachat.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public ApiResponse getProfile(@PathVariable Long userId) {
        ProfileResponse profile = profileService.getProfile(userId);
        return ApiResponse.builder()
                .success(true)
                .message("OK")
                .data(profile)
                .build();
    }

    @PutMapping("/{userId}")
    public ApiResponse updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request) {
        ProfileResponse updatedProfile = profileService.updateProfile(userId, request);
        return ApiResponse.builder()
                .success(true)
                .message("Profile updated successfully")
                .data(updatedProfile)
                .build();
    }
}
