package io.github.opensabre.boot.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Rest注册信息
 */
@Data
@RequiredArgsConstructor
public class RestMappingInfo {
    /**
     * Rest 的path url，如：/user/{name}
     */
    @NonNull
    private String url;
    /**
     * Rest 的方法，如：GET/POST ..
     */
    @NonNull
    private String method;
}
