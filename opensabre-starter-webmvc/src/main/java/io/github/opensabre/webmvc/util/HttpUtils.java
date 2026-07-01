package io.github.opensabre.webmvc.util;

import cn.hutool.core.util.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Get client IP address.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return StringUtils.EMPTY;
        }
        String xForwardedIp = normalizeIp(StringUtils.substringBefore(request.getHeader("X-Forwarded-For"), ","));
        String xRealIp = normalizeIp(request.getHeader("X-Real-IP"));
        return StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(xForwardedIp, xRealIp), request.getRemoteAddr());
    }

    /**
     * Get user agent.
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request != null ? StringUtils.defaultString(request.getHeader("User-Agent")) : StringUtils.EMPTY;
    }

    /**
     * Gen device id.
     */
    public static String getDeviceId(HttpServletRequest request) {
        if (request == null) {
            return StringUtils.EMPTY;
        }
        String userAgent = request.getHeader("User-Agent");
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        return String.valueOf(HashUtil.fnvHash(userAgent + xForwardedFor));
    }

    /**
     * Get current HTTP request.
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Get current HTTP response.
     */
    public static HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }

    private static String normalizeIp(String ip) {
        String trimmedIp = StringUtils.trimToNull(ip);
        return "unknown".equalsIgnoreCase(trimmedIp) ? null : trimmedIp;
    }
}
