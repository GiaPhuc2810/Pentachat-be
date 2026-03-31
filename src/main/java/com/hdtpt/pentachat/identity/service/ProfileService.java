package com.hdtpt.pentachat.identity.service;

import org.springframework.stereotype.Service;
import com.hdtpt.pentachat.identity.model.Profile;
import com.hdtpt.pentachat.identity.repository.ProfileRepository;
import com.hdtpt.pentachat.identity.dto.request.UpdateProfileRequest;
import com.hdtpt.pentachat.identity.dto.response.ProfileResponse;
import com.hdtpt.pentachat.exception.AppException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public ProfileResponse getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Profile not found"));

        return mapToResponse(profile);
    }

    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatar() != null) {
            profile.setAvatar(request.getAvatar());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            profile.setAddress(request.getAddress());
        }
        Profile updatedProfile = profileRepository.save(profile);

        return mapToResponse(updatedProfile);
    }

    public ProfileResponse createProfile(Long userId) {
        Profile profile = Profile.builder()
                .userId(userId)
                .build();

        Profile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile);
    }

    private ProfileResponse mapToResponse(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUserId())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .avatar(profile.getAvatar())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
