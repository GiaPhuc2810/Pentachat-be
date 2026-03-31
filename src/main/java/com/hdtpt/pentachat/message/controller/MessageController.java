package com.hdtpt.pentachat.message.controller;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.message.dto.request.MessageRequest;
import com.hdtpt.pentachat.message.dto.response.MessageResponse;
import com.hdtpt.pentachat.message.service.MessageService;
import com.hdtpt.pentachat.security.SessionManager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Message Controller
 * Handles messaging operations
 * 
 * Endpoints:
 * - POST /message/send : Gửi tin nhắn
 * - GET /message/inbox/{userId} : Lấy inbox
 * - GET /message/conversation/{userId1}/{userId2} : Lấy conversation
 * - POST /message/read/{userId}/{messageId} : Đánh dấu đã đọc
 */
@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Gửi tin nhắn
     * POST /message/send
     * 
     * Input: {"from": "user1", "to": "user2", "content": "hi"}
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendMessage(
            @Valid @RequestBody MessageRequest request) {
        try {
            log.info("Sending message from {} to {}: {}",
                    request.getFrom(), request.getTo(), request.getContent());

            // Gọi hàm pushToUser để gửi tin nhắn
            MessageResponse message = messageService.pushToUser(
                    request.getFrom(),
                    request.getTo(),
                    request.getContent());

            // Kiểm tra user đích có online không
            boolean recipientOnline = SessionManager.isUserOnline(request.getTo());

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Message sent successfully. Recipient online: " + recipientOnline)
                    .data(message)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to send message: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy inbox của user
     * GET /message/inbox/{userId}
     */
    @GetMapping("/inbox/{userId}")
    public ResponseEntity<ApiResponse> getInbox(@PathVariable Long userId) {
        try {
            List<MessageResponse> messages = messageService.getUserInbox(userId);

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Inbox retrieved successfully")
                    .data(messages)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving inbox: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve inbox: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy conversation giữa 2 user
     * GET /message/conversation/{userId1}/{userId2}
     */
    @GetMapping("/conversation/{userId1}/{userId2}")
    public ResponseEntity<ApiResponse> getConversation(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        try {
            List<MessageResponse> messages = messageService.getConversation(userId1, userId2);

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Conversation retrieved successfully")
                    .data(messages)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving conversation: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve conversation: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Đánh dấu message là đã đọc
     * POST /message/read/{userId}/{messageId}
     */
    @PostMapping("/read/{userId}/{messageId}")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long userId,
            @PathVariable Long messageId) {
        try {
            messageService.markAsRead(userId, messageId);

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Message marked as read")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error marking message as read: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to mark message as read: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa message
     * DELETE /message/{userId}/{messageId}
     */
    @DeleteMapping("/{userId}/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
            @PathVariable Long userId,
            @PathVariable Long messageId) {
        try {
            messageService.deleteMessage(userId, messageId);

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Message deleted successfully")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting message: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to delete message: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Test endpoint - Kiểm tra user online status
     * GET /message/status/{userId}
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse> checkUserStatus(@PathVariable Long userId) {
        boolean isOnline = SessionManager.isUserOnline(userId);
        SessionManager.SessionInfo session = SessionManager.getUserSession(userId);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User status retrieved")
                .data(isOnline ? session : "User is offline")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Gửi tin nhắn nhóm
     * POST /api/messages/group/send
     * 
     * Input: {"from": "user1", "groupId": "group123", "content": "hello group"}
     */
    @PostMapping("/group/send")
    public ResponseEntity<ApiResponse> sendGroupMessage(
            @Valid @RequestBody MessageRequest request) {
        try {
            if (request.getGroupId() == null) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.builder()
                                .success(false)
                                .message("groupId is required for group messages")
                                .build());
            }

            log.info("Sending group message to group {} from user {}: {}",
                    request.getGroupId(), request.getFrom(), request.getContent());

            // Gọi hàm pushToGroup để gửi tin nhắn nhóm
            MessageResponse message = messageService.pushToGroup(
                    request.getFrom(),
                    request.getGroupId(),
                    request.getContent());

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Group message sent successfully")
                    .data(message)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending group message: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to send group message: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy lịch sử tin nhắn của một nhóm
     * GET /api/messages/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse> getGroupHistory(@PathVariable Long groupId) {
        try {
            List<MessageResponse> messages = messageService.getGroupHistory(groupId);

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Group history retrieved successfully")
                    .data(messages)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving group history: {}", e.getMessage());

            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve group history: " + e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(response);
        }
    }
}