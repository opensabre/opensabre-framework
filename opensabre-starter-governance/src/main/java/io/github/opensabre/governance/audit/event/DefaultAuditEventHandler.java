package io.github.opensabre.governance.audit.event;

import io.github.opensabre.governance.audit.entity.AuditInfo;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import io.github.opensabre.eda.api.EdaEvent;
import io.github.opensabre.eda.api.EdaEventHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 默认审计事件处理器。
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultAuditEventHandler implements EdaEventHandler<AuditInfo> {

    public static final String EVENT_TYPE = "governance.audit.created";

    private final ObjectProvider<SysadminGovernanceClient> clientProvider;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(EdaEvent<AuditInfo> event) {
        AuditInfo auditInfo = event.payload();
        SysadminGovernanceClient client = clientProvider.getIfAvailable();
        if (client == null) {
            log.debug("Audit event received without sysadmin client: {}", auditInfo);
            return;
        }
        try {
            client.saveAuditLog(auditInfo);
        } catch (Exception e) {
            log.warn("Failed to save audit log to sysadmin", e);
        }
    }
}
