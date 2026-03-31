package com.hdtpt.pentachat.identity.controller;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.identity.dto.request.ChangePasswordRequest;
import com.hdtpt.pentachat.identity.dto.request.LoginRequest;
import com.hdtpt.pentachat.identity.dto.request.RegisterRequest;
import com.hdtpt.pentachat.identity.dto.response.AuthResponse;
import com.hdtpt.pentachat.identity.model.PasswordResetToken;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.repository.PasswordResetTokenRepository;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import com.hdtpt.pentachat.identity.service.AuthService;
import com.hdtpt.pentachat.identity.service.EmailService;
import com.hdtpt.pentachat.security.SessionManager;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    public AuthController(
            AuthService authService,
            EmailService emailService,
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository) {
        this.authService = authService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getUsername(), request.getPassword());

        AuthResponse data = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .sessionId(null)
                .build();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User registered successfully")
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getUsername(), request.getPassword());
        String sessionId = authService.createSession(user);

        AuthResponse data = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .sessionId(sessionId)
                .build();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Login successful")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long authenticatedUserId = getAuthenticatedUser(userId, sessionId);
        authService.changePassword(authenticatedUserId, request.getCurrentPassword(), request.getNewPassword());

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Password changed successfully")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        User user = userRepository.findByUsername(username)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Khong tim thay tai khoan!"));
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Tai khoan chua co email de nhan link dat lai mat khau!"));
        }

        tokenRepository.deleteByUserId(user.getId());

        String tokenString = UUID.randomUUID().toString();
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .token(tokenString)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(tokenEntity);

        emailService.sendResetLink(user.getEmail(), tokenString);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Link dat lai mat khau da gui vao email!"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElse(null);
        if (resetToken == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Link khong hop le hoac da bi xoa!"));
        }

        if (resetToken.isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Link da het han! Vui long yeu cau lai."));
        }

        authService.resetPassword(resetToken.getUserId(), newPassword);
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Doi mat khau thanh cong! Hay dang nhap lai."));
    }

    private Long getAuthenticatedUser(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "User ID and Session ID headers are required.");
        }

        SessionManager.SessionInfo sessionInfo = SessionManager.getUserSession(userId);
        if (sessionInfo == null || !sessionInfo.sessionId.equals(sessionId)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid session.");
        }

        return userId;
    }
}
