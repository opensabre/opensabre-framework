package io.github.opensabre.register.config;

import io.github.opensabre.boot.version.OpensabreVersion;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static io.github.opensabre.boot.config.OpensabreEnvConfig.OPENSABRE_VERSION;

public class Metadata {
    private final Map<String, String> metadata = new HashMap<>();

    public Metadata() {
        metadata.put(OPENSABRE_VERSION, OpensabreVersion.getVersion());
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}