package io.github.opensabre.rpc.loadbalance.balance;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * 负载均衡分发器
 * 主要为工作分发器，从一众节点中选择一个
 */
public interface Balance {
    /**
     * @param instances 所有可用实例
     * @return ServiceInstance
     */
    ServiceInstance choose(List<ServiceInstance> instances);
}
