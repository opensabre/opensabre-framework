package io.github.opensabre.boot.config;

import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.setting.dialect.Props;
import io.github.opensabre.boot.metadata.OpensabreCloud;
import io.github.opensabre.boot.metadata.OpensabreVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.HashMap;

import static io.github.opensabre.boot.metadata.OpensabreCloud.OPENSABRE_CLOUD_AZ;
import static io.github.opensabre.boot.metadata.OpensabreCloud.OPENSABRE_CLOUD_REGION;
import static io.github.opensabre.boot.metadata.OpensabreVersion.OPENSABRE_FORMATTED_VERSION;
import static io.github.opensabre.boot.metadata.OpensabreVersion.OPENSABRE_VERSION;

/**
 * Opensabre环境变更初使化
 * 将版本号初使化到环境变更中，方便应用后续扩展使用
 */
@Slf4j
public class OpensabreEnvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("OpensabreEnvConfig init start");
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(getVersionEnv());
        propertySources.addFirst(getCloudEnv());
        propertySources.addFirst(getGitProperties());
    }

    /**
     * 组装好opensabre框架元数据
     *
     * @return MapPropertySource
     */
    private MapPropertySource getVersionEnv() {
        HashMap<String, Object> versionMap = new HashMap<>();
        versionMap.put(OPENSABRE_VERSION, OpensabreVersion.getVersion());
        versionMap.put(OPENSABRE_FORMATTED_VERSION, OpensabreVersion.getVersionString());
        return new MapPropertySource("version", versionMap);
    }

    /**
     * 组装好opensabre云环境元数据
     *
     * @return MapPropertySource
     */
    private MapPropertySource getCloudEnv() {
        HashMap<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(OPENSABRE_CLOUD_AZ, OpensabreCloud.getCloudAz());
        propertyMap.put(OPENSABRE_CLOUD_REGION, OpensabreCloud.getCloudRegion());
        return new MapPropertySource("cloud", propertyMap);
    }

    /**
     * 加载git.properties构建元数据
     *
     * @return PropertiesPropertySource
     */
    private PropertiesPropertySource getGitProperties() {
        Props props = new Props();
        try {
            props = new Props("git.properties");
        } catch (NoResourceException exception) {
            log.warn("load git properties: {}", exception.getMessage());
        }
        return new PropertiesPropertySource("application", props.toProperties());
    }
}