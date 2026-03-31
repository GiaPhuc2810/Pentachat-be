package com.hdtpt.pentachat.message.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {

    @NotNull(message = "from cannot be null")
    private Long from;

    // For backward compatibility with personal messages
    private Long to;

    // For group messages
    private Long groupId;

    @NotBlank(message = "content cannot be blank")
    private String content;

    // Message type: PERSONAL or GROUP (default: PERSONAL for backward
    // compatibility)
    @Builder.Default
    private String type = "PERSONAL";
}
