package io.github.opensabre.webflux.openapi;

import org.junit.jupiter.api.Test;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.support.NameUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayOpenApiAggregatorTest {

    @Test
    void derivesDocumentsFromLoadBalancedApiRoutesAndDeduplicatesServices() {
        RouteDefinitionLocator locator = () -> Flux.just(
                route("auth-login", "lb://base-authorization", "/oauth2/**,/login"),
                route("auth-api", "lb://base-authorization", "/api/auth/**"),
                route("sys-api", "lb://base-sysadmin", "/api/sysadmin/**"),
                route("external", "https://example.org", "/external/**"));
        var swaggerUi = new SwaggerUiConfigProperties();
        var properties = new OpenApiGatewayProperties();
        properties.setDisplayNames(Map.of("base-authorization", "Authorization"));

        Set<?> urls = new GatewayOpenApiAggregator(locator, swaggerUi, properties).refresh().block();

        assertThat(urls).hasSize(2);
        assertThat(swaggerUi.getUrls().stream().collect(Collectors.toMap(
                url -> url.getName(), url -> url.getUrl())))
                .containsEntry("base-authorization", "/api/auth/v3/api-docs")
                .containsEntry("base-sysadmin", "/api/sysadmin/v3/api-docs");
        assertThat(swaggerUi.getUrls().stream()
                .filter(url -> "base-authorization".equals(url.getName()))
                .findFirst().orElseThrow().getDisplayName()).isEqualTo("Authorization");
    }

    @Test
    void supportsExclusionsAndCustomDocumentPath() {
        RouteDefinitionLocator locator = () -> Flux.just(
                route("admin-api", "lb://base-gateway-admin", "/api/gateway-admin/**"),
                route("iqc-api", "lb://iqc-platform", "/api/iqc/**"));
        var properties = new OpenApiGatewayProperties();
        properties.setExcludedRouteIds(Set.of("admin-api"));
        properties.setApiDocsPath("openapi.json");

        var swaggerUi = new SwaggerUiConfigProperties();
        var urls = new GatewayOpenApiAggregator(locator, swaggerUi, properties).refresh().block();

        assertThat(urls).singleElement().satisfies(url -> {
            assertThat(url.getName()).isEqualTo("iqc-platform");
            assertThat(url.getUrl()).isEqualTo("/api/iqc/openapi.json");
        });
    }

    @Test
    void rebuildsDocumentsWhenGatewayPublishesARouteRefresh() {
        var routes = new AtomicReference<>(List.of(
                route("auth-api", "lb://base-authorization", "/api/auth/**")));
        RouteDefinitionLocator locator = () -> Flux.fromIterable(routes.get());
        var swaggerUi = new SwaggerUiConfigProperties();
        var aggregator = new GatewayOpenApiAggregator(
                locator, swaggerUi, new OpenApiGatewayProperties());
        var listener = new OpensabreGatewayOpenApiAutoConfiguration()
                .opensabreOpenApiRouteRefreshListener(aggregator);

        aggregator.refresh().block();
        routes.set(List.of(route("sys-api", "lb://base-sysadmin", "/api/sysadmin/**")));
        listener.onApplicationEvent(null);

        assertThat(swaggerUi.getUrls())
                .singleElement()
                .satisfies(url -> assertThat(url.getUrl())
                        .isEqualTo("/api/sysadmin/v3/api-docs"));
    }

    private RouteDefinition route(String id, String uri, String paths) {
        RouteDefinition route = new RouteDefinition();
        route.setId(id);
        route.setUri(URI.create(uri));
        PredicateDefinition path = new PredicateDefinition();
        path.setName("Path");
        path.setArgs(Map.of(NameUtils.generateName(0), paths));
        route.setPredicates(List.of(path));
        return route;
    }
}
