package io.github.opensabre.rpc.loadbalance.router;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * 路由算法接口
 * 主要用于筛选实例，从一众节点中筛选出符合要求的节点
 */
public interface Router {
    /**
     * @param instances 所有可用实例
     * @return ServiceInstance列表
     */
    List<ServiceInstance> routing(List<ServiceInstance> instances);
}
