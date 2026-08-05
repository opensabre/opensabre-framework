package io.github.opensabre.governance.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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
    private Registration registration = new Registration();

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
        /** 自动扫描标准字典枚举的包；为空时使用 Spring Boot 应用自动配置包。 */
        private java.util.List<String> scanPackages = java.util.List.of();
    }

    /**
     * Shared runtime policy for best-effort governance registration tasks.
     */
    @Data
    public static class Registration {
        private int maxAttempts = 4;
        private Duration initialBackoff = Duration.ofSeconds(1);
        private Duration maxBackoff = Duration.ofSeconds(30);
        private double jitter = 0.2d;
        private int poolSize = 2;
        private String threadNamePrefix = "governance-registration-";
        private boolean waitForTasksToCompleteOnShutdown = true;
        private Duration awaitTermination = Duration.ofSeconds(35);
    }

    public enum Transport { HTTP, EDA }
}
