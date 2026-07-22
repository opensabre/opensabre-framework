package io.github.opensabre.boot.rest;

import io.github.opensabre.boot.annotations.ResourcePermission;
import io.github.opensabre.boot.entity.RestMappingInfo;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.annotation.Resource;
import java.util.LinkedHashSet;
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
        Set<RestMappingInfo> interfaceInfos = new LinkedHashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : methodMap.entrySet()) {
            RequestMappingInfo requestMappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            if (isFrameworkHandler(handlerMethod)) {
                continue;
            }
            ResourcePermission permission = handlerMethod.getMethodAnnotation(ResourcePermission.class);
            if (permission != null && !permission.register()) {
                continue;
            }
            Set<String> urls = getPatternValues(requestMappingInfo);
            Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods();
            if (methods.isEmpty()) {
                continue;
            }
            Operation operation = handlerMethod.getMethodAnnotation(Operation.class);
            Set<RestMappingInfo> interfaceInfoSet = urls.stream()
                    .flatMap(url -> methods.stream().map(method -> toMappingInfo(
                            url, method, handlerMethod, permission, operation)))
                    .collect(Collectors.toSet());
            interfaceInfos.addAll(interfaceInfoSet);
        }
        return interfaceInfos;
    }

    private Set<String> getPatternValues(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPathPatternsCondition() != null) {
            return mappingInfo.getPathPatternsCondition().getPatternValues();
        }
        if (mappingInfo.getPatternsCondition() != null) {
            return mappingInfo.getPatternsCondition().getPatterns();
        }
        return Set.of();
    }

    private boolean isFrameworkHandler(HandlerMethod handlerMethod) {
        String className = handlerMethod.getBeanType().getName();
        return className.startsWith("org.springframework.") || className.startsWith("org.springdoc.");
    }

    private RestMappingInfo toMappingInfo(String url, RequestMethod method, HandlerMethod handlerMethod,
                                          ResourcePermission permission, Operation operation) {
        String operationName = operation == null ? null : operation.summary();
        String operationDescription = operation == null ? null : operation.description();
        return RestMappingInfo.builder()
                .url(url)
                .method(method.name())
                .code(permission == null ? null : permission.code())
                .name(permission == null
                        ? StringUtils.defaultIfBlank(operationName, handlerMethod.getMethod().getName())
                        : permission.name())
                .type(permission == null ? null : permission.type())
                .description(permission == null ? operationDescription
                        : StringUtils.defaultIfBlank(permission.description(), operationDescription))
                .handlerClass(handlerMethod.getBeanType().getName())
                .handlerMethod(handlerMethod.getMethod().getName())
                .declaredPermission(permission != null)
                .build();
    }
}
