package io.github.opensabre.governance.ratelimit.aspect;

import io.github.opensabre.governance.client.dto.RateLimitCheckRequest;
import io.github.opensabre.governance.client.dto.RateLimitCheckResponse;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.governance.config.GovernanceProperties;
import io.github.opensabre.governance.ratelimit.GovernanceRateLimiter;
import io.github.opensabre.governance.ratelimit.HttpGovernanceRateLimiter;
import io.github.opensabre.governance.ratelimit.annotations.RateLimit;
import io.github.opensabre.governance.ratelimit.enums.RateLimitDimension;
import io.github.opensabre.governance.ratelimit.exception.RateLimitExceededException;
import io.github.opensabre.webmvc.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class RateLimitAspect {

    private final GovernanceRateLimiter rateLimiter;

    /** @deprecated 使用 {@link GovernanceRateLimiter} 构造器，以隔离 HTTP 实现。 */
    @Deprecated
    public RateLimitAspect(SysadminGovernanceClient client, GovernanceProperties properties) {
        this(new HttpGovernanceRateLimiter(client, properties));
    }

    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        if (!rateLimit.enabled()) {
            return joinPoint.proceed();
        }
        RateLimitCheckResponse response = checkLimit(joinPoint, rateLimit);
        if (!response.isAllowed()) {
            setResponseHeaders(response, rateLimit.showRemaining());
            throw new RateLimitExceededException(rateLimit.message(), response.getRemaining(), response.getResetTime());
        }
        setResponseHeaders(response, rateLimit.showRemaining());
        return joinPoint.proceed();
    }

    private RateLimitCheckResponse checkLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        RateLimitCheckRequest request = RateLimitCheckRequest.builder()
                .sceneCode(rateLimit.sceneCode())
                .key(parseKey(joinPoint, rateLimit.key()))
                .keyPrefix(rateLimit.keyPrefix())
                .algorithm(rateLimit.algorithm())
                .dimensions(Arrays.asList(rateLimit.dimensions()))
                .dimensionValues(extractDimensionValues())
                .maxCount(rateLimit.maxCount())
                .period(rateLimit.period())
                .enabled(rateLimit.enabled())
                .build();
        return rateLimiter.check(request).toResponse();
    }

    private Map<RateLimitDimension, String> extractDimensionValues() {
        Map<RateLimitDimension, String> values = new EnumMap<>(RateLimitDimension.class);
        HttpServletRequest request = HttpUtils.getCurrentRequest();
        if (request == null) {
            return values;
        }
        String clientIp = HttpUtils.getClientIpAddress(request);
        values.put(RateLimitDimension.IP, clientIp);
        values.put(RateLimitDimension.DEVICE, StringUtils.defaultIfBlank(request.getHeader("X-Device-Id"),
                HttpUtils.getDeviceId(request)));
        values.put(RateLimitDimension.USER, firstNonBlank(request.getHeader("X-User-Id"), request.getHeader("X-Username")));
        values.put(RateLimitDimension.TENANT, request.getHeader("X-Tenant-Id"));
        values.put(RateLimitDimension.BUSINESS, firstNonBlank(request.getHeader("X-Bid"), request.getParameter("businessKey")));
        return values;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(values)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        if (StringUtils.isBlank(keyExpression)) {
            return StringUtils.EMPTY;
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        try {
            Expression expression = parser.parseExpression(keyExpression);
            Object value = expression.getValue(context);
            return value == null ? StringUtils.EMPTY : value.toString();
        } catch (Exception e) {
            log.warn("Failed to parse rate limit key expression: {}", keyExpression, e);
            return StringUtils.EMPTY;
        }
    }

    private void setResponseHeaders(RateLimitCheckResponse result, boolean showRemaining) {
        if (!showRemaining) {
            return;
        }
        HttpServletResponse response = HttpUtils.getCurrentResponse();
        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.getMaxCount()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(result.getResetTime()));
        }
    }
}
