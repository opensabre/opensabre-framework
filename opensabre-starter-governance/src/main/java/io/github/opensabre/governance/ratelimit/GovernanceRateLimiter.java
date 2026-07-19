package io.github.opensabre.governance.ratelimit;

import io.github.opensabre.governance.client.dto.RateLimitCheckRequest;

/**
 * 限次同步判定门面。EDA 仅可用于其观测数据，不能用于本接口。
 */
public interface GovernanceRateLimiter {

    RateLimitDecision check(RateLimitCheckRequest request);
}
