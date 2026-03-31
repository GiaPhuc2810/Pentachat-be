package com.hdtpt.pentachat.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private Long fromId;

    // For backward compatibility
    private Long toId;

    // For group messages
    private Long targetId; // userId for PERSONAL, groupId for GROUP
    private String type; // PERSONAL or GROUP

    private String content;
    private String fromUsername;
    private String toUsername;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
