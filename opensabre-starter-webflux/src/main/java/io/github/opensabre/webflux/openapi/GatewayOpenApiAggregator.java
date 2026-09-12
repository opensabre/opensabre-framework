package io.github.opensabre.webflux.openapi;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Keeps Springdoc's gateway service list aligned with current load-balanced routes. */
public class GatewayOpenApiAggregator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Log LOGGER = LogFactory.getLog(GatewayOpenApiAggregator.class);
    private static final String PATH_PREDICATE = "Path";
    private static final String OPENAPI_ENABLED = "opensabre.openapi.enabled";
    private static final String OPENAPI_PATH = "opensabre.openapi.path";
    private static final String OPENAPI_NAME = "opensabre.openapi.name";

    private final RouteDefinitionLocator routeDefinitions;
    private final SwaggerUiConfigProperties swaggerUi;
    private final OpenApiGatewayProperties properties;

    public GatewayOpenApiAggregator(RouteDefinitionLocator routeDefinitions,
            SwaggerUiConfigProperties swaggerUi, OpenApiGatewayProperties properties) {
        this.routeDefinitions = routeDefinitions;
        this.swaggerUi = swaggerUi;
        this.properties = properties;
    }

    /** Rebuild the aggregate list from the latest route definitions. */
    public Mono<Set<SwaggerUrl>> refresh() {
        if (!properties.isEnabled()) {
            updateUrls(Set.of());
            return Mono.just(Set.of());
        }
        return routeDefinitions.getRouteDefinitions()
                .filter(this::isDocumentedRoute)
                .sort(Comparator.comparing(RouteDefinition::getId))
                .collectList()
                .map(this::aggregate)
                .doOnNext(urls -> {
                    updateUrls(urls);
                    LOGGER.info("OpenAPI gateway document list refreshed: count=" + urls.size());
                });
    }

    /** Refresh after Spring Cloud Gateway publishes an effective route change. */
    public void onRoutesRefreshed(RefreshRoutesEvent event) {
        refresh().subscribe();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        refresh().block();
    }

    private boolean isDocumentedRoute(RouteDefinition route) {
        URI uri = route.getUri();
        return uri != null
                && "lb".equalsIgnoreCase(uri.getScheme())
                && !properties.getExcludedRouteIds().contains(route.getId())
                && metadataEnabled(route)
                && documentPath(route) != null;
    }

    private Set<SwaggerUrl> aggregate(java.util.List<RouteDefinition> routes) {
        Map<String, DocumentRoute> serviceRoutes = new LinkedHashMap<>();
        for (RouteDefinition route : routes) {
            String serviceId = route.getUri().getHost();
            DocumentRoute candidate = new DocumentRoute(
                    documentPath(route), displayName(route, serviceId), hasMetadataPath(route));
            serviceRoutes.merge(serviceId, candidate, this::preferredRoute);
        }
        Set<SwaggerUrl> urls = new LinkedHashSet<>();
        serviceRoutes.forEach((serviceId, route) -> urls.add(new SwaggerUrl(
                serviceId,
                route.path(),
                route.displayName())));
        return urls;
    }

    private DocumentRoute preferredRoute(DocumentRoute left, DocumentRoute right) {
        if (left.explicit() != right.explicit()) {
            return left.explicit() ? left : right;
        }
        boolean leftApi = left.path().startsWith("/api/");
        boolean rightApi = right.path().startsWith("/api/");
        if (leftApi != rightApi) {
            return leftApi ? left : right;
        }
        return left.path().length() <= right.path().length() ? left : right;
    }

    private boolean metadataEnabled(RouteDefinition route) {
        Object enabled = route.getMetadata().get(OPENAPI_ENABLED);
        return enabled == null || Boolean.parseBoolean(enabled.toString());
    }

    private String documentPath(RouteDefinition route) {
        Object configured = route.getMetadata().get(OPENAPI_PATH);
        if (configured != null && !configured.toString().isBlank()) {
            return normalizePath(configured.toString());
        }
        String prefix = externalPrefix(route);
        return prefix == null ? null : prefix + normalizedDocsPath();
    }

    private boolean hasMetadataPath(RouteDefinition route) {
        Object path = route.getMetadata().get(OPENAPI_PATH);
        return path != null && !path.toString().isBlank();
    }

    private String displayName(RouteDefinition route, String serviceId) {
        Object configured = route.getMetadata().get(OPENAPI_NAME);
        if (configured != null && !configured.toString().isBlank()) {
            return configured.toString();
        }
        return properties.getDisplayNames().getOrDefault(serviceId, serviceId);
    }

    private String externalPrefix(RouteDefinition route) {
        return route.getPredicates().stream()
                .filter(predicate -> PATH_PREDICATE.equalsIgnoreCase(predicate.getName()))
                .flatMap(predicate -> predicate.getArgs().entrySet().stream())
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> isPathPatternKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim)
                .map(this::staticPrefix)
                .filter(prefix -> prefix != null && !prefix.isBlank())
                .findFirst()
                .orElse(null);
    }

    private boolean isPathPatternKey(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.startsWith(NameUtils.GENERATED_NAME_PREFIX)
                || "pattern".equals(normalized)
                || "patterns".equals(normalized)
                || normalized.startsWith("patterns.");
    }

    private String staticPrefix(String pattern) {
        int wildcard = pattern.indexOf('*');
        String prefix = wildcard >= 0 ? pattern.substring(0, wildcard) : pattern;
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix.isBlank() || prefix.contains("{") ? null : prefix;
    }

    private String normalizedDocsPath() {
        return normalizePath(properties.getApiDocsPath());
    }

    private String normalizePath(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }

    private void updateUrls(Set<SwaggerUrl> urls) {
        swaggerUi.setUrls(urls);
    }

    private record DocumentRoute(String path, String displayName, boolean explicit) {
    }
}
