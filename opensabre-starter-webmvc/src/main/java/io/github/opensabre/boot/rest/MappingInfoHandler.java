package io.github.opensabre.boot.rest;

import io.github.opensabre.boot.entity.RestMappingInfo;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取springboot注册的Rest接口处理类
 *
 * @author zhoutaoo
 */
public class MappingInfoHandler {
    /**
     * RequestMappingHandlerMapping类，spring web的Rest注册管理类
     */
    @Resource
    RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 获取spring web应用所有注册的接口服务信息
     *
     * @return Set RestMappingInfo
     */
    public Set<RestMappingInfo> getMappingInfo() {
        // 拿到Handler适配器中的全部方法
        Map<RequestMappingInfo, HandlerMethod> methodMap = requestMappingHandlerMapping.getHandlerMethods();
        Set<RestMappingInfo> interfaceInfos = new HashSet<>();
        for (RequestMappingInfo requestMappingInfo : methodMap.keySet()) {
            Set<String> urls = requestMappingInfo.getPathPatternsCondition().getPatternValues();
            Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods();
            Set<RestMappingInfo> interfaceInfoSet = urls.stream()
                    .flatMap(url -> methods.stream().map(method -> new RestMappingInfo(url, method.name())))
                    .collect(Collectors.toSet());
            interfaceInfos.addAll(interfaceInfoSet);
        }
        return interfaceInfos;
    }
}
