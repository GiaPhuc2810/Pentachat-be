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
public class PokerRoomResponse {
    private Long roomId;
    private Long gameId;
    private Long ownerId;
    private String ownerName;
    private String status;
    private int maxPlayers;
    private int botCount;
    private boolean canStart;
    private List<PokerRoomMemberResponse> members;
    private List<PokerRoomInviteResponse> pendingInvites;
}
