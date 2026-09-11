package io.github.opensabre.monitoring;

import io.github.opensabre.security.actuator.ActuatorMonitoringTokenIssuer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OpensabreMonitoringAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpensabreMonitoringAutoConfiguration.class));

    @Test
    void collectorIsDisabledByDefault() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(PrometheusReadClient.class)
                .doesNotHaveBean(ApplicationMonitoringService.class));
    }

    @Test
    void enabledCollectorDegradesSafelyWhenTokenIssuerIsUnavailable() {
        contextRunner
                .withPropertyValues("opensabre.monitoring.collector.enabled=true")
                .run(context -> assertThat(context)
                        .hasSingleBean(PrometheusReadClient.class)
                        .doesNotHaveBean(ActuatorMetricsClient.class)
                        .doesNotHaveBean(ApplicationMonitoringService.class));
    }

    @Test
    void explicitlyEnabledCollectorCreatesPrometheusClient() {
        contextRunner
                .withBean(ActuatorMonitoringTokenIssuer.class,
                        () -> mock(ActuatorMonitoringTokenIssuer.class))
                .withBean(ObjectMapper.class, () -> mock(ObjectMapper.class))
                .withPropertyValues("opensabre.monitoring.collector.enabled=true")
                .run(context -> assertThat(context)
                        .hasSingleBean(PrometheusReadClient.class)
                        .hasSingleBean(ActuatorMetricsClient.class)
                        .hasSingleBean(ApplicationMonitoringService.class));
    }
}
