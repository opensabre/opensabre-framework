package io.github.opensabre.rpc.openfeign.config;

import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

@LoadBalancerClients(defaultConfiguration = OpensabreLoadBalancerBean.class)
public class OpensabreLoadBalancerConfig {
}