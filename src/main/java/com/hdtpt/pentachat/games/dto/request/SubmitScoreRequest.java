package com.hdtpt.pentachat.games.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitScoreRequest {

    @NotNull
    @Min(0)
    private Integer score;

    @NotNull
    @Min(0)
    @Max(3)
    private Integer livesRemaining;

    @NotNull
    @Min(0)
    private Integer enemiesDestroyed;

    @NotNull
    @Min(0)
    private Integer obstaclesHit;

    @NotNull
    private Boolean gameOver;
}
