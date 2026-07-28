package io.github.opensabre.security.key;

import io.github.opensabre.security.config.InternalTokenProperties;

/**
 * Reads safe key metadata from the application's current shared configuration.
 */
public class PropertiesInternalTokenKeyStatusProvider implements InternalTokenKeyStatusProvider {

    private final InternalTokenProperties properties;

    public PropertiesInternalTokenKeyStatusProvider(InternalTokenProperties properties) {
        this.properties = properties;
    }

    @Override
    public InternalTokenKeyStatus currentStatus() {
        return new InternalTokenKeyStatus(
                properties.isEnabled(),
                properties.getKeyConfigVersion(),
                nullableText(properties.getActiveKeyId()),
                hasText(properties.getActiveKey()),
                nullableText(properties.getPreviousKeyId()),
                hasText(properties.getPreviousKey()),
                properties.getActiveKeyActivatedAt(),
                properties.getPreviousKeyRetireAfter());
    }

    private static String nullableText(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
