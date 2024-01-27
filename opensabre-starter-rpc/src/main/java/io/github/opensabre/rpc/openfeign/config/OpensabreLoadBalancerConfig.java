package io.github.opensabre.rpc.openfeign.config;

import io.github.opensabre.boot.config.YamlPropertyLoaderFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.PropertySource;

/**
 * 负载均衡配置
 */
@AutoConfiguration
@LoadBalancerClients(defaultConfiguration = OpensabreLoadBalancerBean.class)
@PropertySource(value = {"classpath:opensabre-rpc.yml"}, encoding = "UTF8", factory = YamlPropertyLoaderFactory.class)
public class OpensabreLoadBalancerConfig {
}