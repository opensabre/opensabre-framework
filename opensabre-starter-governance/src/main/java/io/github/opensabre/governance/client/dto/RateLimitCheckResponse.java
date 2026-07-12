package io.github.opensabre.governance.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitCheckResponse {
    private boolean allowed;
    private int remaining;
    private long resetTime;
    private String errorMessage;
    private String key;
    private long currentCount;
    private long maxCount;
}
