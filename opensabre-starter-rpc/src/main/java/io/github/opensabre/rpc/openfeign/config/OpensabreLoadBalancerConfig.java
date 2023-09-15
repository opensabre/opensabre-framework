package io.github.opensabre.rpc.openfeign.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

/**
 * 负载均衡配置
 */
@AutoConfiguration
@LoadBalancerClients(defaultConfiguration = OpensabreLoadBalancerBean.class)
public class OpensabreLoadBalancerConfig {
}