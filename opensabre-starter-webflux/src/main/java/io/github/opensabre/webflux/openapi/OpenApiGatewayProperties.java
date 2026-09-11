package io.github.opensabre.webflux.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Configuration for route-driven OpenAPI aggregation at an OpenSabre gateway. */
@ConfigurationProperties("opensabre.openapi.gateway")
public class OpenApiGatewayProperties {

    private boolean enabled = true;
    private String apiDocsPath = "/v3/api-docs";
    private Set<String> excludedRouteIds = new LinkedHashSet<>();
    private Map<String, String> displayNames = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiDocsPath() {
        return apiDocsPath;
    }

    public void setApiDocsPath(String apiDocsPath) {
        this.apiDocsPath = apiDocsPath;
    }

    public Set<String> getExcludedRouteIds() {
        return excludedRouteIds;
    }

    public void setExcludedRouteIds(Set<String> excludedRouteIds) {
        this.excludedRouteIds = excludedRouteIds;
    }

    public Map<String, String> getDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(Map<String, String> displayNames) {
        this.displayNames = displayNames;
    }
}
