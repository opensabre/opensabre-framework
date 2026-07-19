package io.github.opensabre.eda.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenSabre EDA 核心配置。
 */
@Data
@ConfigurationProperties(prefix = "opensabre.eda")
public class EdaProperties {

    private boolean enabled = true;
    private Local local = new Local();
    private Publisher publisher = new Publisher();

    @Data
    public static class Local {
        private int corePoolSize = 2;
        private int maxPoolSize = 8;
        private int queueCapacity = 1_000;
        private String threadNamePrefix = "eda-event-";
        private boolean waitForTasksToCompleteOnShutdown = false;
    }

    @Data
    public static class Publisher {
        /** 远程目标没有可用传输适配器时是否抛出异常。 */
        private boolean remoteRequired = false;
    }
}
