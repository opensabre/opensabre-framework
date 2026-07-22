package io.github.opensabre.boot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rest注册信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestMappingInfo {
    /**
     * Rest 的path url，如：/user/{name}
     */
    private String url;
    /**
     * Rest 的方法，如：GET/POST ..
     */
    private String method;

    private String code;
    private String name;
    private String type;
    private String description;
    private String handlerClass;
    private String handlerMethod;
    private boolean declaredPermission;
}
