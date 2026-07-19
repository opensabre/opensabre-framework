package io.github.opensabre.webmvc.interceptor;

import io.github.opensabre.common.core.util.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserInterceptorTest {

    @AfterEach
    void clearUserContext() {
        UserContextHolder.getInstance().clear();
    }

    @Test
    public void preHandle_当未设置token_user_那么正常处理下一个handle() throws Exception {
        UserInterceptor userInterceptor = new UserInterceptor();
        assertTrue(userInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
    }

    @Test
    public void preHandle_当设置token的username_那么username可以在线程中拿出来用() throws Exception {
        UserInterceptor userInterceptor = new UserInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-client-token-user", "{\"user_name\":\"zhangsan\"}");
        userInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        assertEquals(UserContextHolder.getInstance().getUsername(), "zhangsan");
    }

    @Test
    public void preHandle_当仅传递jwt_那么使用subject作为username() throws Exception {
        UserInterceptor userInterceptor = new UserInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer eyJhbGciOiJub25lIn0.eyJzdWIiOiJ6aGFuZ3NhbiJ9.");

        userInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals("zhangsan", UserContextHolder.getInstance().getUsername());
    }

    @Test
    public void preHandle_当指定用户名解析器_那么使用解析器结果() throws Exception {
        UserInterceptor userInterceptor = new UserInterceptor(request -> "security-user");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-client-token-user", "{\"user_name\":\"zhangsan\"}");

        userInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertEquals("security-user", UserContextHolder.getInstance().getUsername());
    }
}
