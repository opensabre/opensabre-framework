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

    private Usage usage = new Usage();
    private ErrorCatalog errorCatalog = new ErrorCatalog();
    private Dictionary dictionary = new Dictionary();

    @Data
    public static class Sysadmin {
        private String serviceId = "base-sysadmin";
    }

    @Data
    public static class Audit {
        private boolean enabled = true;
        private Async async = new Async();

        /**
         * 审计异步线程池配置。
         */
        @Data
        public static class Async {
            private int corePoolSize = 2;
            private int maxPoolSize = 8;
            private int queueCapacity = 1_000;
            private String threadNamePrefix = "audit-event-";
            private boolean waitForTasksToCompleteOnShutdown = false;
        }
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private boolean failOpen = true;
    }

    @Data
    public static class Usage {
        private boolean enabled = true;
        private Transport transport = Transport.EDA;
    }
    @Data
    public static class ErrorCatalog {
        private boolean enabled = true;
        /** Shared internal credential for error catalog registration. */
        private String registrationToken = "";
    }

    @Data
    public static class Dictionary {
        private boolean enabled = true;
        private boolean registrationEnabled = false;
        private String registrationToken = "";
        private java.util.List<String> preloadCodes = java.util.List.of();
    }

    public enum Transport { HTTP, EDA }
}
