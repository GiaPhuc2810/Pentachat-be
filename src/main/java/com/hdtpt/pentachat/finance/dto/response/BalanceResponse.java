package com.hdtpt.pentachat.finance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Balance response DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceResponse {
    private Long userId;
    private Double balance;
}
