package com.hdtpt.pentachat.games.controller.api;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.games.dto.request.SubmitScoreRequest;
import com.hdtpt.pentachat.games.model.Game;
import com.hdtpt.pentachat.games.model.GameSession;
import com.hdtpt.pentachat.games.service.GameService;
import com.hdtpt.pentachat.security.SessionManager;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllGames() {
        List<Game> games = gameService.getGameList();
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lay danh sach game thanh cong")
                .data(games)
                .build());
    }

    @PostMapping("/{gameId}/sessions/start")
    public ResponseEntity<ApiResponse> startGameSession(
            @PathVariable Long gameId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String sessionId) {

        if (!isValidSession(currentUserId, sessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Bat dau game thanh cong")
                .data(gameService.startGameSession(gameId, currentUserId))
                .build());
    }

    @PostMapping("/{gameId}/sessions/{sessionId}/score")
    public ResponseEntity<ApiResponse> submitScore(
            @PathVariable Long gameId,
            @PathVariable Long sessionId,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-Session-Id") String userSessionId,
            @Valid @RequestBody SubmitScoreRequest request) {

        if (!isValidSession(currentUserId, userSessionId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder().success(false).message("Unauthorized").build());
        }

        GameSession savedSession = gameService.submitScore(gameId, sessionId, currentUserId, request);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Luu diem thanh cong")
                .data(savedSession)
                .build());
    }

    @GetMapping("/{gameId}/leaderboard")
    public ResponseEntity<ApiResponse> getLeaderboard(@PathVariable Long gameId) {
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Lay bang xep hang thanh cong")
                .data(gameService.getLeaderboard(gameId))
                .build());
    }

    private boolean isValidSession(Long userId, String sessionId) {
        SessionManager.SessionInfo session = SessionManager.getUserSession(userId);
        return session != null && session.sessionId.equals(sessionId);
    }
}
