package io.github.opensabre.register.config;

import io.github.opensabre.boot.metadata.OpensabreCloud;
import io.github.opensabre.boot.metadata.OpensabreVersion;

import java.util.HashMap;
import java.util.Map;

import static io.github.opensabre.boot.metadata.OpensabreCloud.OPENSABRE_CLOUD_AZ;
import static io.github.opensabre.boot.metadata.OpensabreCloud.OPENSABRE_CLOUD_REGION;
import static io.github.opensabre.boot.metadata.OpensabreVersion.OPENSABRE_VERSION;

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
        metadata.put(OPENSABRE_CLOUD_AZ, OpensabreCloud.getCloudAz());
        metadata.put(OPENSABRE_CLOUD_REGION, OpensabreCloud.getCloudRegion());
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