package io.github.opensabre.boot.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies compatibility of the mapping metadata API used by legacy listeners. */
class RestMappingInfoCompatibilityTest {

    @Test
    void legacyConstructorShouldRemainAvailable() {
        RestMappingInfo mapping = new RestMappingInfo("/users", "GET");

        assertThat(mapping.getUrl()).isEqualTo("/users");
        assertThat(mapping.getMethod()).isEqualTo("GET");
    }
}
