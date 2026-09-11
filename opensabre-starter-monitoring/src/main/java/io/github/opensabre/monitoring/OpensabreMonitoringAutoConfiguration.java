package io.github.opensabre.monitoring;

import io.github.opensabre.security.actuator.ActuatorMonitoringTokenIssuer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import tools.jackson.databind.ObjectMapper;

/** Auto-configuration for provider defaults and the opt-in control-plane collector. */
@AutoConfiguration
@AutoConfigureAfter(io.github.opensabre.security.config.OpensabreSecurityAutoConfiguration.class)
@PropertySource(value = "classpath:opensabre-monitoring.properties", encoding = "UTF8")
@EnableConfigurationProperties(MonitoringProperties.class)
public class OpensabreMonitoringAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ActuatorMonitoringTokenIssuer.class)
    @ConditionalOnProperty(prefix = "opensabre.monitoring.collector", name = "enabled", havingValue = "true")
    public ActuatorMetricsClient actuatorMetricsClient(ObjectMapper objectMapper,
            ActuatorMonitoringTokenIssuer tokenIssuer, MonitoringProperties properties) {
        return new ActuatorMetricsClient(objectMapper, tokenIssuer, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ActuatorMonitoringTokenIssuer.class)
    @ConditionalOnProperty(prefix = "opensabre.monitoring.collector", name = "enabled", havingValue = "true")
    public ApplicationMonitoringService applicationMonitoringService(ActuatorMetricsClient client) {
        return new ApplicationMonitoringService(client);
    }

    @Bean
    @ConditionalOnMissingBean
    public PrometheusReadClient prometheusReadClient(MonitoringProperties properties) {
        return new PrometheusReadClient(properties);
    }
}
