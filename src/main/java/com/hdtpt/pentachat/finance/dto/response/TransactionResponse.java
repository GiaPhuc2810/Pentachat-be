package com.hdtpt.pentachat.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Transaction response DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private String type;
    private Long fromUserId;
    private Long toUserId;
    private Double amount;
    private LocalDateTime createdAt;
}
