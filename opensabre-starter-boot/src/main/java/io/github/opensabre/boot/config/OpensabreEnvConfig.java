package io.github.opensabre.boot.config;

import io.github.opensabre.boot.metadata.OpensabreCloud;
import io.github.opensabre.boot.metadata.OpensabreVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;

/**
 * Opensabre环境变更初使化
 * 将版本号初使化到环境变更中，方便应用后续扩展使用
 */
@Slf4j
public class OpensabreEnvConfig implements EnvironmentPostProcessor {
    /**
     * Opensabre版本号环境变更Key
     */
    public static final String OPENSABRE_VERSION = "opensabre.version";
    /**
     * Opensabre完整版本号环境变更Key
     */
    public static final String OPENSABRE_FORMATTED_VERSION = "opensabre.formatted-version";
    /**
     * Opensabre云部署AZ环境变量Key
     */
    public static final String OPENSABRE_CLOUD_AZ = "opensabre.cloud.az";
    /**
     * Opensabre云部署REGION环境变量Key
     */
    public static final String OPENSABRE_CLOUD_REGION = "opensabre.cloud.region";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        log.info("OpensabreEnvConfig init start");
        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(getVersionEnv());
        propertySources.addFirst(getCloudEnv());
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
}

