package io.github.opensabre.webflux.openapi;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/** Auto-configures route-driven Knife4j/OpenAPI aggregation for reactive gateways. */
@AutoConfiguration(after = io.github.opensabre.webflux.config.OpensabreWebFluxConfig.class)
@ConditionalOnClass({RouteDefinitionLocator.class, SwaggerUiConfigProperties.class})
@ConditionalOnProperty(prefix = "opensabre.openapi.gateway", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OpenApiGatewayProperties.class)
public class OpensabreGatewayOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GatewayOpenApiAggregator gatewayOpenApiAggregator(RouteDefinitionLocator routeDefinitions,
            SwaggerUiConfigProperties swaggerUi, OpenApiGatewayProperties properties) {
        return new GatewayOpenApiAggregator(routeDefinitions, swaggerUi, properties);
    }

    @Bean
    public ApplicationListener<RefreshRoutesEvent> opensabreOpenApiRouteRefreshListener(
            GatewayOpenApiAggregator aggregator) {
        return aggregator::onRoutesRefreshed;
    }
}
