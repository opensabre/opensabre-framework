package io.github.opensabre.governance.client;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.audit.entity.AuditInfo;
import io.github.opensabre.governance.client.dto.RateLimitCheckRequest;
import io.github.opensabre.governance.client.dto.RateLimitCheckResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${opensabre.governance.sysadmin.service-id:base-sysadmin}", contextId = "sysadminGovernanceClient")
public interface SysadminGovernanceClient {

    @PostMapping("/audit/log")
    Result<Boolean> saveAuditLog(@RequestBody AuditInfo request);

    @PostMapping("/ratelimit/check")
    Result<RateLimitCheckResponse> checkRateLimit(@RequestBody RateLimitCheckRequest request);
}
