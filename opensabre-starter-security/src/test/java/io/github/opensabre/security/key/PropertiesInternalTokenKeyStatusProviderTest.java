package io.github.opensabre.security.key;

import io.github.opensabre.security.config.InternalTokenProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PropertiesInternalTokenKeyStatusProviderTest {

    @Test
    void shouldExposeMetadataWithoutSecretMaterial() {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        properties.setKeyConfigVersion(7);
        properties.setActiveKeyId(" active-7 ");
        properties.setActiveKey("secret-material");
        properties.setActiveKeyActivatedAt(Instant.parse("2026-07-25T00:00:00Z"));

        InternalTokenKeyStatus status =
                new PropertiesInternalTokenKeyStatusProvider(properties).currentStatus();

        assertTrue(status.enabled());
        assertEquals(7, status.configVersion());
        assertEquals("active-7", status.activeKeyId());
        assertTrue(status.activeKeyConfigured());
        assertNull(status.previousKeyId());
        assertFalse(status.previousKeyConfigured());
        assertEquals(Instant.parse("2026-07-25T00:00:00Z"), status.activeKeyActivatedAt());
        assertNull(status.previousKeyRetireAfter());
    }
}
