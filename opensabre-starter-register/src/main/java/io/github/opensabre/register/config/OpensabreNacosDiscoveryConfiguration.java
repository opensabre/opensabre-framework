package io.github.opensabre.register.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 初使化元数据，将元数据注册到注册中心
 */
@Slf4j
@AutoConfiguration
@ConditionalOnNacosDiscoveryEnabled
public class OpensabreNacosDiscoveryConfiguration {

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    /**
     * 将Opensabre元数据注册到注册中心
     */
    @PostConstruct
    public void initNacos() {
        log.info("Opensabre Metadata Register NacosDiscoveryConfig");
        // 原来的元数据全部不变
        nacosDiscoveryProperties.getMetadata().putAll(new Metadata().getMetadata());
    }
}
