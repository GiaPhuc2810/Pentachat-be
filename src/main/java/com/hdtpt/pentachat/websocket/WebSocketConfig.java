package com.hdtpt.pentachat.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP Configuration
 * 
 * Endpoint: /ws - Client kết nối vào đây
 * Subscribe: /topic/messages - Client subscribe để nhận messages
 * Send: /app/chat.send - Client gửi message vào đây
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. Phải thêm "/queue" vào đây thì tin nhắn riêng mới bay đi được
        config.enableSimpleBroker("/topic", "/queue");

        // 2. Prefix cho các message từ client gửi lên (giữ nguyên)
        config.setApplicationDestinationPrefixes("/app");

        // 3. [QUAN TRỌNG] Định nghĩa tiền tố cho tin nhắn riêng tư
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint /ws với SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins cho testing
                .withSockJS(); // Enable SockJS fallback
    }
}
