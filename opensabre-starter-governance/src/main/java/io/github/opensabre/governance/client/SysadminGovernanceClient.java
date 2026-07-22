package io.github.opensabre.governance.client;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.audit.entity.AuditInfo;
import io.github.opensabre.governance.client.dto.RateLimitCheckRequest;
import io.github.opensabre.governance.client.dto.RateLimitCheckResponse;
import io.github.opensabre.governance.errorcatalog.ErrorCatalogSnapshot;
import io.github.opensabre.governance.usage.UsageRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "${opensabre.governance.sysadmin.service-id:base-sysadmin}", contextId = "sysadminGovernanceClient")
public interface SysadminGovernanceClient {

    @PostMapping("/audit/log")
    Result<Boolean> saveAuditLog(@RequestBody AuditInfo request);

    @PostMapping("/ratelimit/check")
    Result<RateLimitCheckResponse> checkRateLimit(@RequestBody RateLimitCheckRequest request);

    @PostMapping("/usage-counters/records")
    Result<Boolean> recordUsage(@RequestBody UsageRecord request);

    @PostMapping("/error-catalog/snapshots")
    Result<Boolean> registerErrorCatalog(@RequestBody ErrorCatalogSnapshot request,
                                         @RequestHeader("X-Opensabre-Error-Catalog-Token") String token);
}
