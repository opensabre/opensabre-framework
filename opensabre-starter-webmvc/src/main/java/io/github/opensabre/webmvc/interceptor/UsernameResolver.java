package io.github.opensabre.webmvc.interceptor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 当前请求用户名解析器。
 */
@FunctionalInterface
public interface UsernameResolver {

    /**
     * 解析当前请求中的用户名。
     *
     * @param request 当前请求
     * @return 用户名，无法解析时返回空字符串
     */
    String resolve(HttpServletRequest request);
}
