package com.hdtpt.pentachat.games.repository;

import com.hdtpt.pentachat.games.model.GameSession;
import com.hdtpt.pentachat.games.model.GameSessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findByIdAndUserId(Long id, Long userId);

    List<GameSession> findTop10ByGameIdAndStatusOrderByScoreDescCreatedAtAsc(Long gameId, GameSessionStatus status);
}
