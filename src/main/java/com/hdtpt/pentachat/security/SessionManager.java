package com.hdtpt.pentachat.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session Manager - Quản lý userId -> Session mapping
 * Giữ track thông tin user nào đang online
 * 
 * Map structure: userId -> Session info
 * Ví dụ: "user1" -> {sessionId: "abc123", isOnline: true}
 */
public class SessionManager {

    // Map để lưu userId -> session info
    private static final Map<Long, SessionInfo> userSessions = new ConcurrentHashMap<>();

    /**
     * Tạo session mới cho user
     */
    public static String createSession(Long userId, String username) {
        String sessionId = "ses_" + java.util.UUID.randomUUID().toString();
        userSessions.put(userId, new SessionInfo(userId, sessionId, true));
        return sessionId;
    }

    /**
     * Lưu session của user
     */
    public static void addUserSession(Long userId, String sessionId) {
        userSessions.put(userId, new SessionInfo(userId, sessionId, true));
    }

    /**
     * Xóa session của user (user logout)
     */
    public static void removeUserSession(Long userId) {
        userSessions.remove(userId);
    }

    /**
     * Lấy session info của user
     */
    public static SessionInfo getUserSession(Long userId) {
        return userSessions.get(userId);
    }

    /**
     * Kiểm tra user có online không
     */
    public static boolean isUserOnline(Long userId) {
        SessionInfo session = userSessions.get(userId);
        return session != null && session.isOnline;
    }

    /**
     * Lấy tất cả active sessions
     */
    public static Map<Long, SessionInfo> getAllSessions() {
        return new ConcurrentHashMap<>(userSessions);
    }

    /**
     * Inner class để lưu session info
     */
    public static class SessionInfo {
        public Long userId;
        public String sessionId;
        public boolean isOnline;

        public SessionInfo(Long userId, String sessionId, boolean isOnline) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.isOnline = isOnline;
        }

        @Override
        public String toString() {
            return "SessionInfo{" +
                    "userId=" + userId +
                    ", sessionId='" + sessionId + '\'' +
                    ", isOnline=" + isOnline +
                    '}';
        }
    }
}
