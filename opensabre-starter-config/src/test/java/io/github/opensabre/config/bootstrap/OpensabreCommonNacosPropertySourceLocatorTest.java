package io.github.opensabre.config.bootstrap;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreCommonNacosPropertySourceLocatorTest {

    @Test
    void shouldLoadCommonYamlDuringBootstrap() {
        OpensabreCommonNacosPropertySourceLocator locator =
                new OpensabreCommonNacosPropertySourceLocator((properties, dataId, group, timeout) -> {
                    assertThat(properties.getProperty("serverAddr")).isEqualTo("rnacos:8848");
                    assertThat(dataId).isEqualTo("opensabre-common.yml");
                    assertThat(group).isEqualTo("DEFAULT_GROUP");
                    assertThat(timeout).isEqualTo(3000);
                    return "spring:\n  security:\n    oauth2:\n      resourceserver:\n        jwt:\n          jwk-set-uri: http://base-authorization:8000/oauth2/jwks\n";
                });

        PropertySource<?> source = locator.locate(new MockEnvironment()
                .withProperty("REGISTER_HOST", "rnacos")
                .withProperty("REGISTER_PORT", "8848"));

        assertThat(source).isNotNull();
        assertThat(source.getProperty("spring.security.oauth2.resourceserver.jwt.jwk-set-uri"))
                .isEqualTo("http://base-authorization:8000/oauth2/jwks");
    }
}
