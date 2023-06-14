package io.github.opensabre.rpc.openfeign.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@AutoConfiguration
@LoadBalancerClients(defaultConfiguration = OpensabreLoadBalancerBean.class)
public class OpensabreLoadBalancerConfig {
}