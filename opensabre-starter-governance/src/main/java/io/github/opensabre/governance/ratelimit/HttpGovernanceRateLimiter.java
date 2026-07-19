package io.github.opensabre.governance.ratelimit;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.client.dto.RateLimitCheckRequest;
import io.github.opensabre.governance.client.dto.RateLimitCheckResponse;
import io.github.opensabre.governance.config.GovernanceProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 HTTP/Feign 的同步限次判定实现。
 */
@Slf4j
public class HttpGovernanceRateLimiter implements GovernanceRateLimiter {

    private final SysadminGovernanceClient client;
    private final GovernanceProperties properties;

    public HttpGovernanceRateLimiter(SysadminGovernanceClient client, GovernanceProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public RateLimitDecision check(RateLimitCheckRequest request) {
        try {
            Result<RateLimitCheckResponse> result = client.checkRateLimit(request);
            RateLimitCheckResponse response = result == null ? null : result.getData();
            if (response != null) {
                return RateLimitDecision.from(response);
            }
            throw new IllegalStateException("Empty sysadmin rate limit response");
        } catch (Exception exception) {
            if (properties.getRatelimit().isFailOpen()) {
                log.warn("Failed to check rate limit from sysadmin, fail-open enabled", exception);
                return new RateLimitDecision(true, request.getMaxCount(),
                        System.currentTimeMillis() + request.getPeriod() * 1000L, request.getMaxCount(), null, request.getKey());
            }
            throw new IllegalStateException("Failed to check rate limit from sysadmin", exception);
        }
    }
}
