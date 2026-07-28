package io.github.opensabre.security.token;

import io.github.opensabre.security.config.InternalTokenProperties;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable active/previous HMAC key snapshot.
 */
final class HmacKeyRing {

    private static final int MINIMUM_KEY_BYTES = 32;

    private final String activeKeyId;
    private final byte[] activeKey;
    private final Map<String, byte[]> verificationKeys;

    HmacKeyRing(InternalTokenProperties properties) {
        this.activeKeyId = requireText(properties.getActiveKeyId(), "active-key-id");
        this.activeKey = decodeKey(properties.getActiveKey(), "active-key");
        Map<String, byte[]> keys = new LinkedHashMap<>();
        keys.put(activeKeyId, activeKey);
        if (hasText(properties.getPreviousKeyId()) || hasText(properties.getPreviousKey())) {
            String previousKeyId = requireText(properties.getPreviousKeyId(), "previous-key-id");
            if (activeKeyId.equals(previousKeyId)) {
                throw invalidConfiguration("active-key-id and previous-key-id must differ");
            }
            keys.put(previousKeyId, decodeKey(properties.getPreviousKey(), "previous-key"));
        }
        this.verificationKeys = Map.copyOf(keys);
    }

    String activeKeyId() {
        return activeKeyId;
    }

    byte[] activeKey() {
        return activeKey.clone();
    }

    byte[] verificationKey(String keyId) {
        byte[] key = verificationKeys.get(keyId);
        return key == null ? null : key.clone();
    }

    private static byte[] decodeKey(String value, String name) {
        String encoded = requireText(value, name);
        try {
            byte[] key = Base64.getDecoder().decode(encoded);
            if (key.length < MINIMUM_KEY_BYTES) {
                throw invalidConfiguration(name + " must decode to at least 32 bytes");
            }
            return key;
        } catch (IllegalArgumentException exception) {
            throw new InternalTokenException(
                    InternalTokenError.INVALID_CONFIGURATION, name + " must be valid Base64", exception);
        }
    }

    private static String requireText(String value, String name) {
        if (!hasText(value)) {
            throw invalidConfiguration(name + " must not be blank");
        }
        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static InternalTokenException invalidConfiguration(String message) {
        return new InternalTokenException(InternalTokenError.INVALID_CONFIGURATION, message);
    }
}
