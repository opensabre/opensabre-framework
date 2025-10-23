package io.github.opensabre.boot.annotations;

import io.github.opensabre.boot.aspect.AuditAspect;
import io.github.opensabre.boot.event.DefaultAuditEventHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 审计日志注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AuditAspect.class, DefaultAuditEventHandler.class})
public @interface EnabledAudit {
}