package com.hdtpt.pentachat.games.service;

import com.hdtpt.pentachat.games.dto.response.PokerRoomInviteResponse;
import com.hdtpt.pentachat.games.dto.response.PokerRoomMemberResponse;
import com.hdtpt.pentachat.games.dto.response.PokerRoomResponse;
import com.hdtpt.pentachat.games.model.InviteStatus;
import com.hdtpt.pentachat.games.model.RoomInvite;
import com.hdtpt.pentachat.games.repository.RoomInviteRepository;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private static final int POKER_MAX_PLAYERS = 4;

    private final RoomInviteRepository inviteRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    private final AtomicLong pokerRoomSequence = new AtomicLong(1000);
    private final Map<Long, PokerRoomState> pokerRooms = new ConcurrentHashMap<>();

    public RoomInvite inviteUser(Long roomId, Long inviterId, Long inviteeId) {
        validateInviteRequest(roomId, inviterId, inviteeId);

        RoomInvite existingInvite = inviteRepository.findByRoomIdAndInviteeId(roomId, inviteeId).orElse(null);
        if (existingInvite != null) {
            if (existingInvite.getStatus() == InviteStatus.PENDING) {
                throw new RuntimeException("Invite is already pending");
            }
            if (existingInvite.getStatus() == InviteStatus.ACCEPTED) {
                throw new RuntimeException("User is already in the room");
            }

            existingInvite.setInviterId(inviterId);
            existingInvite.setStatus(InviteStatus.PENDING);
            RoomInvite resentInvite = inviteRepository.save(existingInvite);
            messagingTemplate.convertAndSend("/topic/notifications." + inviteeId, resentInvite);
            messagingTemplate.convertAndSend("/topic/room." + roomId, "INVITE_SENT");
            log.info("Invite resent for room {} from {} to {}", roomId, inviterId, inviteeId);
            return resentInvite;
        }

        RoomInvite invite = RoomInvite.builder()
                .roomId(roomId)
                .inviterId(inviterId)
                .inviteeId(inviteeId)
                .status(InviteStatus.PENDING)
                .build();

        RoomInvite savedInvite = inviteRepository.save(invite);
        messagingTemplate.convertAndSend("/topic/notifications." + inviteeId, savedInvite);
        messagingTemplate.convertAndSend("/topic/room." + roomId, "INVITE_SENT");
        log.info("Invite saved for room {} from {} to {}", roomId, inviterId, inviteeId);
        return savedInvite;
    }

    public Map<String, Object> getRoomMembers(Long roomId) {
        List<RoomInvite> invites = inviteRepository.findByRoomId(roomId);
        Map<String, Object> response = new HashMap<>();

        if (invites.isEmpty()) {
            return response;
        }

        Long ownerId = invites.get(0).getInviterId();
        String ownerName = getUsername(ownerId);

        Set<Long> userIds = new HashSet<>();
        userIds.add(ownerId);
        invites.stream()
                .filter(invite -> invite.getStatus() == InviteStatus.ACCEPTED)
                .forEach(invite -> userIds.add(invite.getInviteeId()));

        List<String> memberNames = userRepository.findAllById(userIds).stream()
                .map(user -> user.getUsername())
                .collect(Collectors.toList());

        response.put("owner", ownerName);
        response.put("members", memberNames);
        return response;
    }

    public void acceptInvite(Long inviteId) {
        RoomInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay loi moi voi ID: " + inviteId));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new RuntimeException("Invite is not pending");
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);
        log.info("Invite {} accepted by user {} for room {}", inviteId, invite.getInviteeId(), invite.getRoomId());
    }

    public void leaveRoom(Long roomId, Long userId) {
        List<RoomInvite> invites = inviteRepository.findByRoomId(roomId);
        if (invites.isEmpty()) {
            return;
        }

        Long ownerId = invites.get(0).getInviterId();
        if (ownerId.equals(userId)) {
            invites.forEach(inviteRepository::delete);
            log.info("Owner {} closed room {}", userId, roomId);
            return;
        }

        invites.stream()
                .filter(invite -> invite.getInviteeId().equals(userId))
                .forEach(inviteRepository::delete);
        log.info("User {} left room {}", userId, roomId);
    }

    public PokerRoomResponse createPokerRoom(Long gameId, Long ownerId) {
        if (gameId == null) {
            throw new RuntimeException("Game ID is required");
        }

        Long roomId = pokerRoomSequence.incrementAndGet();
        PokerRoomState room = PokerRoomState.builder()
                .roomId(roomId)
                .gameId(gameId)
                .ownerId(ownerId)
                .status("WAITING")
                .maxPlayers(POKER_MAX_PLAYERS)
                .botCount(0)
                .members(new LinkedHashMap<>())
                .build();
        room.getMembers().put(ownerId, PokerRoomMemberState.builder()
                .userId(ownerId)
                .username(getUsername(ownerId))
                .owner(true)
                .status("JOINED")
                .build());
        pokerRooms.put(roomId, room);
        return toPokerRoomResponse(room);
    }

    public PokerRoomResponse getPokerRoom(Long gameId, Long roomId) {
        PokerRoomState room = getExistingPokerRoom(roomId);
        if (!room.getGameId().equals(gameId)) {
            throw new RuntimeException("Room does not belong to this game");
        }
        return toPokerRoomResponse(room);
    }

    public PokerRoomResponse inviteToPokerRoom(Long gameId, Long roomId, Long inviterId, Long inviteeId) {
        PokerRoomState room = getExistingPokerRoom(roomId);
        validatePokerRoom(room, gameId);
        if (!room.getOwnerId().equals(inviterId)) {
            throw new RuntimeException("Only the room owner can invite players");
        }
        if (room.getMembers().containsKey(inviteeId)) {
            throw new RuntimeException("User is already in the room");
        }
        if (room.getMembers().size() >= room.getMaxPlayers()) {
            throw new RuntimeException("Room is already full");
        }

        RoomInvite invite = inviteUser(roomId, inviterId, inviteeId);
        PokerRoomInviteResponse payload = toPokerInviteResponse(invite, room.getGameId());
        messagingTemplate.convertAndSend("/topic/poker-invites." + inviteeId, payload);
        broadcastPokerRoom(roomId);
        return toPokerRoomResponse(room);
    }

    public List<PokerRoomInviteResponse> getPendingPokerInvites(Long gameId, Long userId) {
        return inviteRepository.findByInviteeIdAndStatus(userId, InviteStatus.PENDING).stream()
                .filter(invite -> {
                    PokerRoomState room = pokerRooms.get(invite.getRoomId());
                    return room != null && room.getGameId().equals(gameId);
                })
                .sorted(Comparator.comparing(RoomInvite::getCreatedAt).reversed())
                .map(invite -> {
                    PokerRoomState room = pokerRooms.get(invite.getRoomId());
                    return toPokerInviteResponse(invite, room.getGameId());
                })
                .collect(Collectors.toList());
    }

    public PokerRoomResponse joinPokerRoom(Long gameId, Long roomId, Long currentUserId, Long inviteId) {
        PokerRoomState room = getExistingPokerRoom(roomId);
        validatePokerRoom(room, gameId);
        RoomInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        if (!invite.getRoomId().equals(roomId)) {
            throw new RuntimeException("Invite does not belong to this room");
        }
        if (!invite.getInviteeId().equals(currentUserId)) {
            throw new RuntimeException("This invite does not belong to the current user");
        }
        if (room.getMembers().size() >= room.getMaxPlayers()) {
            throw new RuntimeException("Room is already full");
        }

        acceptInvite(inviteId);
        room.getMembers().put(currentUserId, PokerRoomMemberState.builder()
                .userId(currentUserId)
                .username(getUsername(currentUserId))
                .owner(false)
                .status("JOINED")
                .build());
        broadcastPokerRoom(roomId);
        return toPokerRoomResponse(room);
    }

    public PokerRoomResponse leavePokerRoom(Long gameId, Long roomId, Long currentUserId) {
        PokerRoomState room = getExistingPokerRoom(roomId);
        validatePokerRoom(room, gameId);

        if (room.getOwnerId().equals(currentUserId)) {
            closePokerRoom(roomId);
            return PokerRoomResponse.builder()
                    .roomId(roomId)
                    .gameId(gameId)
                    .ownerId(currentUserId)
                    .ownerName(getUsername(currentUserId))
                    .status("CLOSED")
                    .maxPlayers(POKER_MAX_PLAYERS)
                    .botCount(0)
                    .canStart(false)
                    .members(List.of())
                    .pendingInvites(List.of())
                    .build();
        }

        room.getMembers().remove(currentUserId);
        inviteRepository.findByRoomIdAndInviteeId(roomId, currentUserId)
                .ifPresent(inviteRepository::delete);
        broadcastPokerRoom(roomId);
        return toPokerRoomResponse(room);
    }

    public PokerRoomResponse startPokerRoom(Long gameId, Long roomId, Long currentUserId, Integer botCount) {
        PokerRoomState room = getExistingPokerRoom(roomId);
        validatePokerRoom(room, gameId);
        if (!room.getOwnerId().equals(currentUserId)) {
            throw new RuntimeException("Only the room owner can start the table");
        }
        int requestedBotCount = Math.max(0, botCount == null ? 0 : botCount);
        int maxAllowedBots = Math.max(0, room.getMaxPlayers() - room.getMembers().size());
        int appliedBotCount = Math.min(requestedBotCount, maxAllowedBots);
        if (room.getMembers().size() + appliedBotCount < 2) {
            throw new RuntimeException("Need at least 2 seats filled by players or bots to start");
        }
        room.setBotCount(appliedBotCount);
        room.setStatus("READY");
        broadcastPokerRoom(roomId);
        return toPokerRoomResponse(room);
    }

    public void broadcastPokerRoom(Long roomId) {
        PokerRoomState room = getExistingPokerRoom(roomId);
        messagingTemplate.convertAndSend("/topic/poker-room." + roomId, toPokerRoomResponse(room));
    }

    private void closePokerRoom(Long roomId) {
        pokerRooms.remove(roomId);
        inviteRepository.findByRoomId(roomId).forEach(inviteRepository::delete);
        messagingTemplate.convertAndSend("/topic/poker-room." + roomId, Map.of("status", "CLOSED", "roomId", roomId));
    }

    private void validateInviteRequest(Long roomId, Long inviterId, Long inviteeId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }
        if (inviterId == null || inviteeId == null) {
            throw new RuntimeException("Inviter ID and invitee ID are required");
        }
        if (inviterId.equals(inviteeId)) {
            throw new RuntimeException("Cannot invite yourself");
        }
        if (userRepository.findById(inviterId).isEmpty()) {
            throw new RuntimeException("Inviter does not exist");
        }
        if (userRepository.findById(inviteeId).isEmpty()) {
            throw new RuntimeException("Invitee does not exist");
        }
    }

    private void validatePokerRoom(PokerRoomState room, Long gameId) {
        if (!room.getGameId().equals(gameId)) {
            throw new RuntimeException("Room does not belong to this game");
        }
        if ("CLOSED".equals(room.getStatus())) {
            throw new RuntimeException("Room is closed");
        }
    }

    private PokerRoomState getExistingPokerRoom(Long roomId) {
        PokerRoomState room = pokerRooms.get(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found");
        }
        return room;
    }

    private PokerRoomResponse toPokerRoomResponse(PokerRoomState room) {
        List<PokerRoomMemberResponse> members = new ArrayList<>(room.getMembers().values()).stream()
                .map(member -> PokerRoomMemberResponse.builder()
                        .userId(member.getUserId())
                        .username(member.getUsername())
                        .owner(member.isOwner())
                        .status(member.getStatus())
                        .build())
                .collect(Collectors.toList());

        List<PokerRoomInviteResponse> pendingInvites = inviteRepository.findByRoomIdAndStatus(room.getRoomId(), InviteStatus.PENDING).stream()
                .map(invite -> toPokerInviteResponse(invite, room.getGameId()))
                .collect(Collectors.toList());

        return PokerRoomResponse.builder()
                .roomId(room.getRoomId())
                .gameId(room.getGameId())
                .ownerId(room.getOwnerId())
                .ownerName(getUsername(room.getOwnerId()))
                .status(room.getStatus())
                .maxPlayers(room.getMaxPlayers())
                .botCount(room.getBotCount())
                .canStart(members.size() + room.getBotCount() >= 2)
                .members(members)
                .pendingInvites(pendingInvites)
                .build();
    }

    private PokerRoomInviteResponse toPokerInviteResponse(RoomInvite invite, Long gameId) {
        return PokerRoomInviteResponse.builder()
                .inviteId(invite.getId())
                .roomId(invite.getRoomId())
                .gameId(gameId)
                .inviterId(invite.getInviterId())
                .inviterName(getUsername(invite.getInviterId()))
                .inviteeId(invite.getInviteeId())
                .status(invite.getStatus().name())
                .build();
    }

    private String getUsername(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getUsername())
                .orElse("Unknown");
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class PokerRoomState {
        private Long roomId;
        private Long gameId;
        private Long ownerId;
        private String status;
        private int maxPlayers;
        private int botCount;
        private Map<Long, PokerRoomMemberState> members;
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class PokerRoomMemberState {
        private Long userId;
        private String username;
        private boolean owner;
        private String status;
    }
}
