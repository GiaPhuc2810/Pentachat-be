package com.hdtpt.pentachat.games.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetDefinitionResponse {
    private String code;
    private String name;
    private Integer points;
    private Integer health;
    private Integer spawnWeight;
    private Double sizeMultiplier;
    private String iconUrl;
}
