package io.github.opensabre.governance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opensabre.governance")
public class GovernanceProperties {

    private boolean enabled = true;

    private Sysadmin sysadmin = new Sysadmin();

    private Audit audit = new Audit();

    private RateLimit ratelimit = new RateLimit();

    @Data
    public static class Sysadmin {
        private String serviceId = "base-sysadmin";
    }

    @Data
    public static class Audit {
        private boolean enabled = true;
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private boolean failOpen = true;
    }
}
