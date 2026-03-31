package com.hdtpt.pentachat.games.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePointWalletResponse {
    private Long userId;
    private Integer totalPoints;
    private Integer availablePoints;
    private Integer totalGemsConverted;
    private Integer exchangeThreshold;
}
