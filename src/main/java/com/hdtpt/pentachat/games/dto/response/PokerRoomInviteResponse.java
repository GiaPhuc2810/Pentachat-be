package com.hdtpt.pentachat.games.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PokerRoomInviteResponse {
    private Long inviteId;
    private Long roomId;
    private Long gameId;
    private Long inviterId;
    private String inviterName;
    private Long inviteeId;
    private String status;
}
