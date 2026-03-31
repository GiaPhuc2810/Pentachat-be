package com.hdtpt.pentachat.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.identity.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);
}
