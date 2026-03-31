package com.hdtpt.pentachat.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ChatMessage DTO - for real-time chat events
 * Used in WebSocket/realtime communication
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private String from;
    private String to;
    private String content;
    private LocalDateTime timestamp;
}
