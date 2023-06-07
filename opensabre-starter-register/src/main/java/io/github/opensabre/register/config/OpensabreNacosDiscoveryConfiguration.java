package io.github.opensabre.register.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.discovery.NacosWatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 初使化元数据，将元数据注册到注册中心
 */
@Slf4j
@AutoConfiguration
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureBefore({NacosDiscoveryClientConfiguration.class})
public class OpensabreNacosDiscoveryConfiguration {
    /**
     * 将Opensabre元数据注册到注册中心
     *
     * @param nacosServiceManager      nacos服务管理对象
     * @param nacosDiscoveryProperties nacos服务发现配置对象
     * @return NacosWatch
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.watch.enabled", matchIfMissing = true)
    public NacosWatch nacosWatch(NacosServiceManager nacosServiceManager,
                                 NacosDiscoveryProperties nacosDiscoveryProperties) {
        log.info("Opensabre Metadata Register NacosDiscoveryConfig");
        //原来的元数据全部不变
        nacosDiscoveryProperties.getMetadata().putAll(new Metadata().getMetadata());
        return new NacosWatch(nacosServiceManager, nacosDiscoveryProperties);
    }
}
