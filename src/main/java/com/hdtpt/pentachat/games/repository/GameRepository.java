package com.hdtpt.pentachat.games.repository;

import com.hdtpt.pentachat.games.model.Game;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByNameIgnoreCase(String name);
}
