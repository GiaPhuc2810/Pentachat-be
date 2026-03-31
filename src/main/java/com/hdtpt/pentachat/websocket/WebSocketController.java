package com.hdtpt.pentachat.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.hdtpt.pentachat.message.dto.MessageDTO;

/**
 * WebSocket Controller
 * 
 * Nhận messages từ client qua /app/chat.send
 * Broadcast messages tới tất cả subscribers tại /topic/messages
 */
@Controller
@Slf4j
public class WebSocketController {

    /**
     * Nhận message từ client tại /app/chat.send
     * Broadcast tới /topic/messages
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public MessageDTO sendMessage(MessageDTO message) {
        log.info("📨 WebSocket message received:");
        log.info("   From: {}", message.getFrom());
        log.info("   To: {}", message.getTo());
        log.info("   Content: {}", message.getContent());
        log.info("   Timestamp: {}", message.getTimestamp());

        // Broadcast message tới tất cả subscribers
        return message;
    }
}
