package com.hdtpt.pentachat.identity.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long messageCount;
    private long friendCount;
    private long groupCount;
    private Double walletBalance;
}
