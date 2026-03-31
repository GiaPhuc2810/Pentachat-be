package com.hdtpt.pentachat.games.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private Integer rank;
    private Long userId;
    private String username;
    private Integer score;
    private Integer gemsAwarded;
    private Integer enemiesDestroyed;
    private LocalDateTime finishedAt;
}
