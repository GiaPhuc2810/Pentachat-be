package com.hdtpt.pentachat.games.controller.api;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.games.model.RoomInvite;
import com.hdtpt.pentachat.games.service.RoomService;
import com.hdtpt.pentachat.security.SessionManager;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomApiController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/{roomId}/accept/{inviteId}")
    public ResponseEntity<ApiResponse> acceptInvite(
            @PathVariable Long roomId,
            @PathVariable Long inviteId) {

        // 1. Cập nhật trạng thái ACCEPTED trong database
        roomService.acceptInvite(inviteId);

        // 2. [QUAN TRỌNG] Bắn tín hiệu "UPDATE_MEMBERS" tới kênh của phòng
        // Mọi máy đang subscribe kênh này sẽ nhận được thông báo
        messagingTemplate.convertAndSend("/topic/room." + roomId, "UPDATE_MEMBERS");

        return ResponseEntity.ok(ApiResponse.builder().success(true).build());
    }

    @PostMapping("/{roomId}/invite/{inviteeId}")
    public ResponseEntity<ApiResponse> inviteUser(
            @PathVariable Long roomId,
            @PathVariable Long inviteeId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        // Xác thực session tương tự phần Chat
        SessionManager.SessionInfo session = SessionManager.getUserSession(currentUserId);
        if (session == null || !session.sessionId.equals(sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RoomInvite invite = roomService.inviteUser(roomId, currentUserId, inviteeId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Đã gửi lời mời!")
                .data(invite)
                .build());
    }

    @GetMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse> getRoomMembers(@PathVariable Long roomId) {
        // 1. Đổi List<String> thành Object vì Service trả về Map<String, Object>
        Object roomData = roomService.getRoomMembers(roomId);

        // 2. Trả về cho Frontend (ApiResponse sẽ tự bọc cái Map này lại)
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .data(roomData)
                .build());
    }

    // 3. Thêm API Thoát phòng để xử lý cập nhật khi có người out
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse> leaveRoom(
            @PathVariable Long roomId,
            @RequestHeader("X-User-Id") Long userId) {

        roomService.leaveRoom(roomId, userId);

        // Bắn tín hiệu để những người còn lại tự động load lại danh sách
        messagingTemplate.convertAndSend("/topic/room." + roomId, "USER_LEFT");

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đã rời phòng").build());
    }
}
