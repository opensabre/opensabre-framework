package io.github.opensabre.governance.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 验证治理 Starter 的公共上报凭据与字典注册默认配置。 */
class GovernanceDefaultPropertiesTest {

    @Test
    void sharesGovernanceTokenAndEnablesDictionaryRegistration() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource(
                "governance-token", Map.of("GOVERNANCE_REGISTRATION_TOKEN", "shared-token")));
        new YamlPropertySourceLoader().load(
                        "opensabre-governance", new ClassPathResource("opensabre-governance.yml"))
                .forEach(environment.getPropertySources()::addLast);

        assertEquals("shared-token", environment.getProperty(
                "opensabre.governance.error-catalog.registration-token"));
        assertEquals("shared-token", environment.getProperty(
                "opensabre.governance.dictionary.registration-token"));
        assertEquals(Boolean.TRUE, environment.getProperty(
                "opensabre.governance.dictionary.registration-enabled", Boolean.class));
    }
}
