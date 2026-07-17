package io.github.opensabre.governance.audit.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.eda.api.EdaEvent;
import io.github.opensabre.eda.api.EdaEventPublisher;
import io.github.opensabre.governance.audit.annotations.Audit;
import io.github.opensabre.governance.audit.entity.AuditInfo;
import io.github.opensabre.governance.audit.event.DefaultAuditEventHandler;
import io.github.opensabre.webmvc.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 审计日志切面
 */
@Slf4j
@Aspect
public class AuditAspect {

    private final ObjectMapper objectMapper;
    private final EdaEventPublisher eventPublisher;

    public AuditAspect(ObjectMapper objectMapper, EdaEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        Object result = null;
        Throwable exception = null;
        long startTime = System.currentTimeMillis();
        String keyValue = getKeyValue(joinPoint, audit.key());
        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            // 构建审计日志
            long executionTime = System.currentTimeMillis() - startTime;
            AuditInfo auditinfo = buildAuditInfo(joinPoint.getArgs(), audit, result, exception, executionTime);
            auditinfo.setTargetKey(keyValue);
            // 事件通知
            EdaEvent<AuditInfo> auditEvent = EdaEvent.of(DefaultAuditEventHandler.EVENT_TYPE, "governance", auditinfo);
            eventPublisher.publishLocal(auditEvent);
            log.info("Audit event published: eventId={}", auditEvent.eventId());
        }
        return result;
    }

    /**
     * 构建审计日志
     */
    private AuditInfo buildAuditInfo(Object[] args, Audit audit,
                                     Object result, Throwable exception, long executionTime) {
        // 获取请求信息
        HttpServletRequest request = HttpUtils.getCurrentRequest();
        // 构建审计日志
        return AuditInfo.builder()
                .operationType(audit.operationType()) // 操作类型
                .description(audit.description())     // 操作描述
                .operationTime(new Date())   // 操作时间
                .module(audit.module()) // 模块
                .operatorUsername(StringUtils.defaultIfBlank(UserContextHolder.getInstance().getUsername(), "system")) // 操作人用户名
                .clientIp(HttpUtils.getClientIpAddress(request))   // 客户端ip
                .userAgent(HttpUtils.getUserAgent(request)) // User-Agent
                .requestMethod(request != null ? request.getMethod() : StringUtils.EMPTY)  // 请求方法GET/POST ...
                .requestUrl(request != null ? request.getRequestURI() : StringUtils.EMPTY) // 请求url
                .request(audit.request() ? getRequestParams(args) : StringUtils.EMPTY) // 请求参数
                .response(audit.response() ? getResponseResult(result) : StringUtils.EMPTY)   // 返回结果
                .errorMessage(exception != null ? exception.getMessage() : StringUtils.EMPTY) // 错误信息
                .executionTime(executionTime)       // 操作耗时
                .build();
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(Object[] params) {
        return serialize(params);
    }

    /**
     * 获取返回结果
     */
    private String getResponseResult(Object result) {
        return serialize(result);
    }

    /**
     * 获取目标关键的值，支持SpEL表达式
     */
    private String getKeyValue(ProceedingJoinPoint joinPoint, String key) {
        // key为空，直接返回内容为空
        if (StringUtils.isBlank(key)) {
            return StringUtils.EMPTY;
        }
        // 创建解析器
        ExpressionParser parser = new SpelExpressionParser();
        // 创建上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        try {
            // 参数名
            String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            // 参数值
            Object[] args = joinPoint.getArgs();
            // 将参数转为Map结构
            Map<String, Object> params = IntStream.range(0, parameterNames.length)
                    .boxed()
                    .collect(Collectors.toMap(i -> parameterNames[i], i -> args[i]));
            // 设置参数
            context.setVariables(params);
            // 从请求参数中获取 key指定的值，返回
            return serialize(parser.parseExpression(key).getValue(context));
        } catch (Exception e) {
            log.warn("获取目标key的值失败", e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 序列化对象
     */
    private String serialize(Object obj) {
        if (Objects.isNull(obj)) {
            return StringUtils.EMPTY;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("序列化对象失败", e);
            return "serialize error";
        }
    }
}
