package io.github.opensabre.governance.client.dto;

import io.github.opensabre.governance.ratelimit.enums.RateLimitAlgorithmType;
import io.github.opensabre.governance.ratelimit.enums.RateLimitDimension;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitCheckRequest {
    private String sceneCode;
    private String key;
    private String keyPrefix;
    private RateLimitAlgorithmType algorithm;
    private List<RateLimitDimension> dimensions;
    private Map<RateLimitDimension, String> dimensionValues;
    private int maxCount;
    private int period;
    private boolean enabled;
}
