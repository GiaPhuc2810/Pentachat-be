package com.hdtpt.pentachat.games.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSessionResponse {
    private Long sessionId;
    private Long gameId;
    private String gameName;
    private String mode;
    private Integer initialLives;
    private Integer arenaWidth;
    private Integer arenaHeight;
    private String shipAssetUrl;
    private Integer bulletIntervalMs;
    private Integer targetSpawnIntervalMs;
    private Integer obstacleSpawnIntervalMs;
    private Integer gemThresholdScore;
    private Integer boardSize;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer maxBots;
    private Boolean botsSupported;
    private List<String> defaultBotNames;
    private String statusMessage;
    private List<TargetDefinitionResponse> targets;
    private List<ObstacleDefinitionResponse> obstacles;
}
