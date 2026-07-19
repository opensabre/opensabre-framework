package io.github.opensabre.governance.usage;

import io.github.opensabre.governance.client.SysadminGovernanceClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 通过 Sysadmin HTTP 接口受理使用量记录。远端故障不会改变调用方主营业务结果。
 */
@Slf4j
public class HttpUsageCounterRecorder implements UsageCounterRecorder {

    private final SysadminGovernanceClient client;

    public HttpUsageCounterRecorder(SysadminGovernanceClient client) {
        this.client = client;
    }

    @Override
    public void record(UsageRecord record) {
        try {
            client.recordUsage(record);
        } catch (Exception exception) {
            log.warn("Failed to submit usage record through HTTP: objectType={}, objectId={}, eventType={}",
                    record.objectType(), record.objectId(), record.eventType(), exception);
        }
    }
}
