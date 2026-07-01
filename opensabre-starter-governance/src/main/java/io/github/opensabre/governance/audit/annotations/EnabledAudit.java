package io.github.opensabre.governance.audit.annotations;

import io.github.opensabre.governance.audit.aspect.AuditAspect;
import io.github.opensabre.governance.audit.event.DefaultAuditEventHandler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AuditAspect.class, DefaultAuditEventHandler.class})
public @interface EnabledAudit {
}
