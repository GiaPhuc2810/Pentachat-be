package com.hdtpt.pentachat.games.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObstacleDefinitionResponse {
    private String code;
    private String name;
    private Integer damage;
    private String iconUrl;
    private String imageUrl;
}
