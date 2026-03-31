package com.hdtpt.pentachat.games.model;

import com.hdtpt.pentachat.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer livesRemaining;

    @Column(nullable = false)
    private Integer enemiesDestroyed;

    @Column(nullable = false)
    private Integer obstaclesHit;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer gemsAwarded;

    @Column(nullable = false, columnDefinition = "bit default 0")
    private Boolean rewardGranted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSessionStatus status;

    @Column
    private LocalDateTime finishedAt;
}
