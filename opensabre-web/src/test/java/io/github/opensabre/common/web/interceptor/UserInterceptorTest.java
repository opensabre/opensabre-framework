package io.github.opensabre.common.web.interceptor;

import io.github.opensabre.common.core.util.UserContextHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserInterceptorTest {
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
}