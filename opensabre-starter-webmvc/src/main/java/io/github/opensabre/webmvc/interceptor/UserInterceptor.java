package io.github.opensabre.webmvc.interceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户信息拦截器
 */
public class UserInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UsernameResolver usernameResolver;

    /**
     * 服务间调用token用户信息,格式为json
     * {
     * "user_name":"必须有"
     * "自定义key:"value"
     * }
     */
    public static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    /**
     * 用户名的上下文键。
     */
    public static final String USERNAME_KEY = "user_name";
    /**
     * 服务间调用的认证token
     */
    public static final String X_CLIENT_TOKEN = "x-client-token";

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
        //从网关获取并校验,通过校验就可信任x-client-token-user中的信息
        checkToken(request.getHeader(X_CLIENT_TOKEN));
        UserContextHolder.getInstance().setContext(getUserContext(request));
        return true;
    }

    /**
     * 构建当前请求的用户上下文。
     *
     * @param request 当前请求
     * @return 用户上下文
     */
    private Map<String, String> getUserContext(HttpServletRequest request) throws Exception {
        Map<String, String> userContext = new HashMap<>();
        String userInfoString = request.getHeader(X_CLIENT_TOKEN_USER);
        if (StringUtils.isNotBlank(userInfoString)) {
            userContext.putAll(objectMapper.readValue(userInfoString, new TypeReference<>() {
            }));
        }
        String username = usernameResolver.resolve(request);
        if (StringUtils.isNotBlank(username)) {
            userContext.put(USERNAME_KEY, username);
        } else {
            userContext.remove(USERNAME_KEY);
        }
        return userContext;
    }

    /**
     * 校验Token
     *
     * @param token 传来的token
     */
    private void checkToken(String token) {
        //TODO 从网关获取并校验,通过校验就可信任x-client-token-user中的信息
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        UserContextHolder.getInstance().clear();
    }
}
