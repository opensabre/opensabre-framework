package io.github.opensabre.common.web.interceptor;

import com.google.common.collect.Maps;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * spring cloud feign传递header
 *
 * @author zhoutaoo
 */
@Component
public class FeignHeaderInterceptor implements RequestInterceptor {

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
     * @return Map<String, String> header map
     */
    private Map<String, String> getHeaders() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Map<String, String> map = Maps.newHashMap();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}
