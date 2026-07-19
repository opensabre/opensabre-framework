package io.github.opensabre.governance.ratelimit;

import io.github.opensabre.governance.client.dto.RateLimitCheckResponse;

/**
 * 同步限次判定结果。
 */
public record RateLimitDecision(boolean allowed, int remaining, long resetTime, long maxCount,
                                String errorMessage, String key) {

    public static RateLimitDecision from(RateLimitCheckResponse response) {
        return new RateLimitDecision(response.isAllowed(), response.getRemaining(), response.getResetTime(),
                response.getMaxCount(), response.getErrorMessage(), response.getKey());
    }

    public RateLimitCheckResponse toResponse() {
        return RateLimitCheckResponse.builder().allowed(allowed).remaining(remaining).resetTime(resetTime)
                .maxCount(maxCount).errorMessage(errorMessage).key(key).build();
    }
}
