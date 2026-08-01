package io.github.opensabre.config.bootstrap;

import com.alibaba.nacos.api.NacosFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 在 Spring Cloud Bootstrap 阶段加载 OpenSabre 的 Nacos 公共配置。
 *
 * <p>不能使用普通 AutoConfiguration 的 {@code @PropertySource} 声明共享配置；
 * 该阶段晚于 Nacos Bootstrap，导致 Data ID 不会被订阅。</p>
 */
public class OpensabreCommonNacosPropertySourceLocator implements PropertySourceLocator {

    private static final String CONFIG_NAME = "opensabreCommonNacos";
    private static final Logger log = LoggerFactory.getLogger(OpensabreCommonNacosPropertySourceLocator.class);
    private final NacosCommonConfigLoader configLoader;

    public OpensabreCommonNacosPropertySourceLocator() {
        this((properties, dataId, group, timeout) ->
                NacosFactory.createConfigService(properties).getConfig(dataId, group, timeout));
    }

    OpensabreCommonNacosPropertySourceLocator(NacosCommonConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        String dataId = environment.getProperty("OPENSABRE_COMMON_CONFIG_DATA_ID", "opensabre-common.yml");
        String group = environment.getProperty("OPENSABRE_COMMON_CONFIG_GROUP", "DEFAULT_GROUP");
        String serverAddr = environment.getProperty("spring.cloud.nacos.config.server-addr",
                environment.getProperty("REGISTER_HOST", "localhost") + ":"
                        + environment.getProperty("REGISTER_PORT", "8848"));
        try {
            Properties properties = new Properties();
            properties.setProperty("serverAddr", serverAddr);
            String content = configLoader.load(properties, dataId, group, 3000);
            if (!StringUtils.hasText(content)) {
                return null;
            }
            CompositePropertySource source = new CompositePropertySource(CONFIG_NAME);
            for (PropertySource<?> propertySource : new YamlPropertySourceLoader().load(CONFIG_NAME,
                    new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)))) {
                source.addFirstPropertySource(propertySource);
            }
            log.info("Loaded OpenSabre Nacos common config {}/{} during Bootstrap", group, dataId);
            return source;
        }
        catch (Exception ex) {
            throw new IllegalStateException("Failed to load Nacos common config " + group + "/" + dataId, ex);
        }
    }

    @FunctionalInterface
    interface NacosCommonConfigLoader {
        String load(Properties properties, String dataId, String group, long timeout) throws Exception;
    }
}
