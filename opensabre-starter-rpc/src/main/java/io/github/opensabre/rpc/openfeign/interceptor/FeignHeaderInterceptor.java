package io.github.opensabre.rpc.openfeign.interceptor;

import com.google.common.collect.Maps;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * spring cloud feign传递header
 *
 * @author zhoutaoo
 */
public class FeignHeaderInterceptor implements RequestInterceptor {

    private static final Set<String> NON_FORWARDABLE_HEADERS = Set.of(
            "authorization", "x-client-token", "x-client-token-user");

    /**
     * 获取request header 放入远程template中
     */
    @Override
    public void apply(RequestTemplate template) {
        getHeaders().forEach(template::header);
    }

    /**
     * 获取 request 中的所有的 header 值
     *
     * @return header map
     */
    private Map<String, String> getHeaders() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return Collections.emptyMap();
        }
        HttpServletRequest request = attributes.getRequest();
        Map<String, String> map = Maps.newHashMap();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return map;
        }
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            if (!NON_FORWARDABLE_HEADERS.contains(key.toLowerCase())) {
                map.put(key, value);
            }
        }
        return map;
    }
}
