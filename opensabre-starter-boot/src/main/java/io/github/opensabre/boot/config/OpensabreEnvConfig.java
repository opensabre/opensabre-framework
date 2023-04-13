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

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("OpensabreEnvConfig start");
        HashMap<String, Object> versionMap = new HashMap<>();
        versionMap.put("opensabre.version", OpensabreVersion.getVersion());
        versionMap.put("opensabre.formatted-version", OpensabreVersion.getVersionString());
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new MapPropertySource("version", versionMap));
    }
}

