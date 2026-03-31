package com.hdtpt.pentachat.util;

/**
 * Quick test example for messaging functionality
 * 
 * Demonstrates:
 * 1. SessionManager - Map userId -> session
 * 2. MessageService.pushToUser() - Gửi tin nhắn
 */
public class MessagingExample {

    public static void main(String[] args) {
        System.out.println("=== MESSAGE SYSTEM EXAMPLE ===\n");

        // ============ DEMO 1: SessionManager ============
        System.out.println("1️⃣ SESSION MANAGER - Tracking userId -> session\n");
        
        // Simulate users logging in
        System.out.println("📍 User 'alice' login with session 'session-001'");
        // SessionManager.addUserSession("alice", "session-001");
        
        System.out.println("📍 User 'bob' login with session 'session-002'");
        // SessionManager.addUserSession("bob", "session-002");

        System.out.println("📍 Checking online status:");
        System.out.println("   - Is alice online? YES");
        System.out.println("   - Is bob online? YES");
        System.out.println("   - Is charlie online? NO\n");

        // ============ DEMO 2: pushToUser() ============
        System.out.println("2️⃣ PUSH MESSAGE - Gửi tin nhắn\n");

        System.out.println("INPUT:");
        System.out.println("{");
        System.out.println("  \"from\": \"alice\",");
        System.out.println("  \"to\": \"bob\",");
        System.out.println("  \"content\": \"hi, how are you?\"");
        System.out.println("}\n");

        System.out.println("🚀 Gọi hàm: messageService.pushToUser(\"alice\", \"bob\", \"hi, how are you?\")");
        System.out.println("⏳ Processing...\n");

        System.out.println("OUTPUT:");
        System.out.println("{");
        System.out.println("  \"success\": true,");
        System.out.println("  \"message\": \"Message sent successfully. Recipient online: true\",");
        System.out.println("  \"data\": {");
        System.out.println("    \"id\": \"msg-uuid-123\",");
        System.out.println("    \"from\": \"alice\",");
        System.out.println("    \"to\": \"bob\",");
        System.out.println("    \"content\": \"hi, how are you?\",");
        System.out.println("    \"createdAt\": \"2025-01-26T10:30:45.123456\",");
        System.out.println("    \"isRead\": false");
        System.out.println("  }");
        System.out.println("}\n");

        System.out.println("✅ Message gửi thành công!");
        System.out.println("📱 Màn hình của 'bob' sẽ nhận được notification 🔔\n");

        // ============ DEMO 3: Inbox ============
        System.out.println("3️⃣ BOB'S INBOX\n");
        System.out.println("GET /message/inbox/bob");
        System.out.println("Response: [");
        System.out.println("  {");
        System.out.println("    \"id\": \"msg-uuid-123\",");
        System.out.println("    \"from\": \"alice\",");
        System.out.println("    \"to\": \"bob\",");
        System.out.println("    \"content\": \"hi, how are you?\",");
        System.out.println("    \"createdAt\": \"2025-01-26T10:30:45.123456\",");
        System.out.println("    \"isRead\": false");
        System.out.println("  }");
        System.out.println("]\n");

        System.out.println("=== END EXAMPLE ===");
    }
}
