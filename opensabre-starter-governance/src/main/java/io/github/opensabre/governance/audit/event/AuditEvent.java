package io.github.opensabre.governance.audit.event;

import org.springframework.context.ApplicationEvent;

/**
 * 审计事件
 */
public class AuditEvent extends ApplicationEvent {
    public AuditEvent(Object source) {
        super(source);
    }
}
