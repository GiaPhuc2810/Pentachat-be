package com.hdtpt.pentachat.games.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PokerRoomMemberResponse {
    private Long userId;
    private String username;
    private boolean owner;
    private String status;
}
