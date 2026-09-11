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
                && externalPrefix(route) != null;
    }

    private Set<SwaggerUrl> aggregate(java.util.List<RouteDefinition> routes) {
        Map<String, String> servicePaths = new LinkedHashMap<>();
        for (RouteDefinition route : routes) {
            String serviceId = route.getUri().getHost();
            String path = externalPrefix(route);
            servicePaths.merge(serviceId, path, this::preferredPath);
        }
        Set<SwaggerUrl> urls = new LinkedHashSet<>();
        servicePaths.forEach((serviceId, prefix) -> urls.add(new SwaggerUrl(
                serviceId,
                prefix + normalizedDocsPath(),
                properties.getDisplayNames().getOrDefault(serviceId, serviceId))));
        return urls;
    }

    private String preferredPath(String left, String right) {
        boolean leftApi = left.startsWith("/api/");
        boolean rightApi = right.startsWith("/api/");
        if (leftApi != rightApi) {
            return leftApi ? left : right;
        }
        return left.length() <= right.length() ? left : right;
    }

    private String externalPrefix(RouteDefinition route) {
        return route.getPredicates().stream()
                .filter(predicate -> PATH_PREDICATE.equalsIgnoreCase(predicate.getName()))
                .flatMap(predicate -> predicate.getArgs().entrySet().stream())
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT)
                                .startsWith(NameUtils.GENERATED_NAME_PREFIX)
                        || "pattern".equalsIgnoreCase(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(value -> java.util.Arrays.stream(value.split(",")))
                .map(String::trim)
                .map(this::staticPrefix)
                .filter(prefix -> prefix != null && !prefix.isBlank())
                .findFirst()
                .orElse(null);
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
        String path = properties.getApiDocsPath();
        return path.startsWith("/") ? path : "/" + path;
    }

    private void updateUrls(Set<SwaggerUrl> urls) {
        swaggerUi.setUrls(urls);
    }
}
