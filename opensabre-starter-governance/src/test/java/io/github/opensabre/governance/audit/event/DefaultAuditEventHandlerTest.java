package io.github.opensabre.governance.audit.event;

import io.github.opensabre.common.core.entity.vo.Result;
import io.github.opensabre.governance.audit.entity.AuditInfo;
import io.github.opensabre.governance.client.SysadminGovernanceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.scheduling.annotation.Async;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultAuditEventHandlerTest {

    @Test
    void shouldSaveAuditEventAndKeepAsyncAnnotation() throws NoSuchMethodException {
        SysadminGovernanceClient client = mock(SysadminGovernanceClient.class);
        when(client.saveAuditLog(any())).thenReturn(Result.success(true));
        DefaultAuditEventHandler handler = new DefaultAuditEventHandler(clientProvider(client));

        handler.onApplicationEvent(new AuditEvent(AuditInfo.builder().module("test").build()));

        verify(client).saveAuditLog(any(AuditInfo.class));
        assertNotNull(DefaultAuditEventHandler.class.getMethod("onApplicationEvent", AuditEvent.class)
                .getAnnotation(Async.class));
    }

    @Test
    void shouldIgnoreClientFailure() {
        SysadminGovernanceClient client = mock(SysadminGovernanceClient.class);
        doThrow(new IllegalStateException("sysadmin unavailable")).when(client).saveAuditLog(any());
        DefaultAuditEventHandler handler = new DefaultAuditEventHandler(clientProvider(client));

        assertDoesNotThrow(() -> handler.onApplicationEvent(new AuditEvent(AuditInfo.builder().build())));
    }

    private ObjectProvider<SysadminGovernanceClient> clientProvider(SysadminGovernanceClient client) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("sysadminGovernanceClient", client);
        return beanFactory.getBeanProvider(SysadminGovernanceClient.class);
    }
}
