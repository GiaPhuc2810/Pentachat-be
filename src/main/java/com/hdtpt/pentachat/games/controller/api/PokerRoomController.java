package com.hdtpt.pentachat.games.controller.api;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.games.service.RoomService;
import com.hdtpt.pentachat.security.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/{gameId}/poker")
@RequiredArgsConstructor
public class PokerRoomController {

    private final RoomService roomService;

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse> createRoom(
            @PathVariable Long gameId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Tao phong Poker thanh cong")
                .data(roomService.createPokerRoom(gameId, currentUserId))
                .build());
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse> getRoom(
            @PathVariable Long gameId,
            @PathVariable Long roomId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lay phong Poker thanh cong")
                .data(roomService.getPokerRoom(gameId, roomId))
                .build());
    }

    @GetMapping("/invites/pending/{userId}")
    public ResponseEntity<ApiResponse> getPendingInvites(
            @PathVariable Long gameId,
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId) || !currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lay loi moi Poker thanh cong")
                .data(roomService.getPendingPokerInvites(gameId, userId))
                .build());
    }

    @PostMapping("/rooms/{roomId}/invite/{inviteeId}")
    public ResponseEntity<ApiResponse> invitePlayer(
            @PathVariable Long gameId,
            @PathVariable Long roomId,
            @PathVariable Long inviteeId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Da gui loi moi vao phong Poker")
                .data(roomService.inviteToPokerRoom(gameId, roomId, currentUserId, inviteeId))
                .build());
    }

    @PostMapping("/rooms/{roomId}/join/{inviteId}")
    public ResponseEntity<ApiResponse> joinRoom(
            @PathVariable Long gameId,
            @PathVariable Long roomId,
            @PathVariable Long inviteId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Da vao phong Poker")
                .data(roomService.joinPokerRoom(gameId, roomId, currentUserId, inviteId))
                .build());
    }

    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse> leaveRoom(
            @PathVariable Long gameId,
            @PathVariable Long roomId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Da roi phong Poker")
                .data(roomService.leavePokerRoom(gameId, roomId, currentUserId))
                .build());
    }

    @PostMapping("/rooms/{roomId}/start")
    public ResponseEntity<ApiResponse> startRoom(
            @PathVariable Long gameId,
            @PathVariable Long roomId,
            @RequestParam(name = "botCount", required = false) Integer botCount,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Phong Poker san sang bat dau")
                .data(roomService.startPokerRoom(gameId, roomId, currentUserId, botCount))
                .build());
    }

    private boolean isValidSession(Long userId, String sessionId) {
        SessionManager.SessionInfo session = SessionManager.getUserSession(userId);
        return session != null && session.sessionId.equals(sessionId);
    }
}
