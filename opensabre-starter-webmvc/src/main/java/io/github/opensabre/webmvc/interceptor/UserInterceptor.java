package io.github.opensabre.webmvc.interceptor;

import io.github.opensabre.common.core.util.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户信息拦截器
 */
public class UserInterceptor implements HandlerInterceptor {

    private static final String BOUND_ATTRIBUTE = UserInterceptor.class.getName() + ".BOUND";

    private final UsernameResolver usernameResolver;

    /**
     * 用户名的上下文键。
     */
    public static final String USERNAME_KEY = "user_name";

    /**
     * 使用默认用户名解析器创建拦截器。
     */
    public UserInterceptor() {
        this(new DefaultUsernameResolver());
    }

    /**
     * 使用指定用户名解析器创建拦截器。
     *
     * @param usernameResolver 用户名解析器
     */
    public UserInterceptor(UsernameResolver usernameResolver) {
        this.usernameResolver = usernameResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // The security starter owns signed x-client-token handling. Never overwrite its trusted context.
        if (!UserContextHolder.getInstance().getContext().isEmpty()) {
            return true;
        }
        Map<String, String> context = getUserContext(request);
        if (!context.isEmpty()) {
            UserContextHolder.getInstance().setContext(context);
            request.setAttribute(BOUND_ATTRIBUTE, Boolean.TRUE);
        }
        return true;
    }

    /**
     * 构建当前请求的用户上下文。
     *
     * @param request 当前请求
     * @return 用户上下文
     */
    private Map<String, String> getUserContext(HttpServletRequest request) throws Exception {
        Map<String, String> userContext = new LinkedHashMap<>();
        String username = usernameResolver.resolve(request);
        if (StringUtils.isNotBlank(username)) {
            userContext.put(USERNAME_KEY, username);
        } else {
            userContext.remove(USERNAME_KEY);
        }
        return userContext;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        if (Boolean.TRUE.equals(request.getAttribute(BOUND_ATTRIBUTE))) {
            UserContextHolder.getInstance().clear();
        }
    }
}
