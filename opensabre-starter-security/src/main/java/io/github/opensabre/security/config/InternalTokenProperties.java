package io.github.opensabre.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Shared internal token configuration loaded by every OpenSabre application.
 */
@Data
@ConfigurationProperties(prefix = "opensabre.security.internal-token")
public class InternalTokenProperties {

    private boolean enabled = true;
    /**
     * Whether every servlet request must carry an internal token.
     * Keep disabled for applications that also receive external JWT requests.
     */
    private boolean required;
    private long keyConfigVersion;
    private String activeKeyId = "";
    /**
     * Base64-encoded active HMAC key. At least 32 decoded bytes are required.
     */
    private String activeKey = "";
    private String previousKeyId = "";
    /**
     * Base64-encoded previous HMAC key retained during the rotation window.
     */
    private String previousKey = "";
    private Instant activeKeyActivatedAt;
    private Instant previousKeyRetireAfter;
    private Duration ttl = Duration.ofSeconds(60);
    private Duration maxTtl = Duration.ofSeconds(120);
    private Duration clockSkew = Duration.ofSeconds(5);
    private int maxHop = 8;
    private int maxTokenBytes = 8192;
    private int maxExtensionBytes = 2048;
    private Set<String> allowedIssuers = new LinkedHashSet<>();
    private Set<String> allowedExtensionKeys = new LinkedHashSet<>();
    private Set<String> excludedPaths = new LinkedHashSet<>(Set.of(
            "/actuator/**", "/v3/**", "/doc.html", "/webjars/**", "/assets/**"));
}
