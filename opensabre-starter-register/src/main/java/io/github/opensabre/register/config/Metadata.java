package io.github.opensabre.register.config;

import io.github.opensabre.boot.version.OpensabreVersion;

import java.util.HashMap;
import java.util.Map;

import static io.github.opensabre.boot.config.OpensabreEnvConfig.OPENSABRE_VERSION;

/**
 * 应用元数据类
 */
public class Metadata {
    /**
     * 元数据存储容器
     */
    private final Map<String, String> metadata = new HashMap<>();

    /**
     * 构建方法内初使化元数据
     */
    public Metadata() {
        metadata.put(OPENSABRE_VERSION, OpensabreVersion.getVersion());
    }

    /**
     * 获取全部元数据
     *
     * @return all metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
}