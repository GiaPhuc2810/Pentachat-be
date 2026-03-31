package com.hdtpt.pentachat.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * FriendRequestDTO - Data Transfer Object cho friend request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDTO {

    private Long id;

    @NotNull(message = "fromUserId cannot be null")
    private Long fromUserId;

    private Long toUserId;

    private String fromUsername;

    private String status;

    private String toUsername;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
