package io.github.opensabre.config.bootstrap;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreNacosBootstrapRegistrationTest {

    @Test
    void shouldRegisterBootstrapConfigurationInSpringFactories() throws Exception {
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/spring.factories");
        boolean registered = false;
        while (resources.hasMoreElements()) {
            try (InputStream input = resources.nextElement().openStream()) {
                Properties properties = new Properties();
                properties.load(input);
                String configurations = properties.getProperty("org.springframework.cloud.bootstrap.BootstrapConfiguration", "");
                registered |= configurations.contains(OpensabreNacosBootstrapConfiguration.class.getName());
            }
        }
        assertThat(registered).isTrue();
    }
}
