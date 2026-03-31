package com.hdtpt.pentachat.identity.repository;

import com.hdtpt.pentachat.identity.model.PasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(Long userId); // Xóa token cũ nếu có
}