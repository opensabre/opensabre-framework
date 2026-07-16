package io.github.opensabre.governance.audit.event;

import io.github.opensabre.governance.audit.entity.AuditInfo;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

/**
 * 默认 AuditEvent Listener
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultAuditEventHandler implements ApplicationListener<AuditEvent> {

    private final ObjectProvider<SysadminGovernanceClient> clientProvider;

    @Override
    @Async("auditTaskExecutor")
    public void onApplicationEvent(AuditEvent event) {
        if (!(event.getSource() instanceof AuditInfo auditInfo)) {
            log.warn("Unsupported audit event source: {}", event.getSource());
            return;
        }
        SysadminGovernanceClient client = clientProvider.getIfAvailable();
        if (client == null) {
            log.debug("AuditEvent received without sysadmin client: {}", auditInfo);
            return;
        }
        try {
            client.saveAuditLog(auditInfo);
        } catch (Exception e) {
            log.warn("Failed to save audit log to sysadmin", e);
        }
    }
}
