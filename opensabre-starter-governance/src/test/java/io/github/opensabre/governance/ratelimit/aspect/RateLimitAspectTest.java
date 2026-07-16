package io.github.opensabre.governance.ratelimit.aspect;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.client.dto.RateLimitCheckResponse;
import io.github.opensabre.governance.config.GovernanceProperties;
import io.github.opensabre.governance.ratelimit.annotations.RateLimit;
import io.github.opensabre.governance.ratelimit.exception.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RateLimitAspectTest {

    @Test
    void shouldProceedWhenSysadminAllowsRequest() throws Throwable {
        SysadminGovernanceClient client = mock(SysadminGovernanceClient.class);
        when(client.checkRateLimit(any())).thenReturn(Result.success(RateLimitCheckResponse.builder()
                .allowed(true).remaining(4).maxCount(5).resetTime(1L).build()));
        ProceedingJoinPoint joinPoint = joinPoint("order-1", "ok");

        Object result = new RateLimitAspect(client, new GovernanceProperties()).around(joinPoint, rateLimit());

        assertEquals("ok", result);
    }

    @Test
    void shouldRejectWhenSysadminDeniesRequest() throws Throwable {
        SysadminGovernanceClient client = mock(SysadminGovernanceClient.class);
        when(client.checkRateLimit(any())).thenReturn(Result.success(RateLimitCheckResponse.builder()
                .allowed(false).remaining(0).maxCount(5).resetTime(1L).build()));

        RateLimit rateLimit = rateLimit();
        ProceedingJoinPoint joinPoint = joinPoint("order-1", "ok");

        assertThrows(RateLimitExceededException.class,
                () -> new RateLimitAspect(client, new GovernanceProperties()).around(joinPoint, rateLimit));
    }

    @Test
    void shouldFailOpenWhenSysadminIsUnavailable() throws Throwable {
        SysadminGovernanceClient client = mock(SysadminGovernanceClient.class);
        when(client.checkRateLimit(any())).thenThrow(new IllegalStateException("sysadmin unavailable"));
        GovernanceProperties properties = new GovernanceProperties();
        properties.getRatelimit().setFailOpen(true);

        Object result = new RateLimitAspect(client, properties).around(joinPoint("order-1", "ok"), rateLimit());

        assertEquals("ok", result);
    }

    private ProceedingJoinPoint joinPoint(String orderId, Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{orderId});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getParameterNames()).thenReturn(new String[]{"orderId"});
        when(joinPoint.proceed()).thenReturn(result);
        return joinPoint;
    }

    private RateLimit rateLimit() throws NoSuchMethodException {
        Method method = RateLimitAspectTest.class.getDeclaredMethod("limitedOperation", String.class);
        return method.getAnnotation(RateLimit.class);
    }

    @RateLimit(sceneCode = "TEST", key = "#orderId")
    private void limitedOperation(String orderId) {
    }
}
