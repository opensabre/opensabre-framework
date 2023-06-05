package io.github.opensabre.boot.config;

import io.github.opensabre.boot.version.OpensabreVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;

@Slf4j
public class OpensabreEnvConfig implements EnvironmentPostProcessor {

    public static final String OPENSABRE_VERSION = "opensabre.version";
    public static final String OPENSABRE_FORMATTED_VERSION = "opensabre.formatted-version";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("OpensabreEnvConfig start");
        HashMap<String, Object> versionMap = new HashMap<>();
        versionMap.put(OPENSABRE_VERSION, OpensabreVersion.getVersion());
        versionMap.put(OPENSABRE_FORMATTED_VERSION, OpensabreVersion.getVersionString());
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new MapPropertySource("version", versionMap));
    }
}

