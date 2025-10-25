package io.github.opensabre.boot.event;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

/**
 *  默认 AuditEvent Listener
 */
@Slf4j
public class DefaultAuditEventHandler implements ApplicationListener<AuditEvent> {

    @Override
    public void onApplicationEvent(@NonNull AuditEvent event) {
        log.info("AuditEvent received: {}", event);
    }
}