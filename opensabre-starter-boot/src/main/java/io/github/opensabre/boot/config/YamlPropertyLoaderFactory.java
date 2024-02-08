package io.github.opensabre.boot.config;

import lombok.NonNull;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * 加载yaml的Factory
 */
public class YamlPropertyLoaderFactory extends DefaultPropertySourceFactory {

    @Override
    public @NonNull PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        return new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource()).get(0);
    }
}