package com.hdtpt.pentachat.games.controller.api;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.games.dto.request.ExchangePointsRequest;
import com.hdtpt.pentachat.games.service.GamePointWalletService;
import com.hdtpt.pentachat.security.SessionManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/points")
@RequiredArgsConstructor
public class GamePointWalletController {

    private final GamePointWalletService gamePointWalletService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getWallet(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId) || !currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lay vi diem thanh cong")
                .data(gamePointWalletService.getWallet(userId))
                .build());
    }

    @PostMapping("/{userId}/exchange")
    public ResponseEntity<ApiResponse> exchangePoints(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId,
            @Valid @RequestBody ExchangePointsRequest request) {

        if (!isValidSession(currentUserId, sessionId) || !currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Doi diem thanh gem thanh cong")
                .data(gamePointWalletService.exchangePoints(userId, request.getPoints()))
                .build());
    }

    private boolean isValidSession(Long userId, String sessionId) {
        SessionManager.SessionInfo session = SessionManager.getUserSession(userId);
        return session != null && session.sessionId.equals(sessionId);
    }
}
