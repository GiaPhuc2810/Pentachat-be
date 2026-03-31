package com.hdtpt.pentachat.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hdtpt.pentachat.identity.model.Profile;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);
}
