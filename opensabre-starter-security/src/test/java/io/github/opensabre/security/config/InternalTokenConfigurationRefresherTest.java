package io.github.opensabre.security.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalTokenConfigurationRefresherTest {

    @Test
    void atomicallyAppliesValidHigherVersion() {
        InternalTokenProperties live = initial();
        InternalTokenConfigurationRefresher refresher = refresher(live);

        refresher.refresh(yaml(2, "key-2", key((byte) 2)));

        InternalTokenProperties snapshot = live.snapshot();
        assertThat(snapshot.getKeyConfigVersion()).isEqualTo(2);
        assertThat(snapshot.getActiveKeyId()).isEqualTo("key-2");
        assertThat(refresher.currentStatus().successful()).isTrue();
    }

    @Test
    void rejectsInvalidKeyAndKeepsLastGoodSnapshot() {
        InternalTokenProperties live = initial();
        InternalTokenConfigurationRefresher refresher = refresher(live);

        refresher.refresh(yaml(2, "key-2", "dG9vLXNob3J0"));

        assertThat(live.getKeyConfigVersion()).isEqualTo(1);
        assertThat(live.getActiveKeyId()).isEqualTo("key-1");
        assertThat(refresher.currentStatus().successful()).isFalse();
    }

    @Test
    void rejectsEmptyConfigurationAndKeepsLastGoodSnapshot() {
        InternalTokenProperties live = initial();
        InternalTokenConfigurationRefresher refresher = refresher(live);

        refresher.refresh("");

        assertThat(live.getKeyConfigVersion()).isEqualTo(1);
        assertThat(live.getActiveKeyId()).isEqualTo("key-1");
        assertThat(refresher.currentStatus().successful()).isFalse();
        assertThat(refresher.currentStatus().message()).contains("internal-token configuration is missing");
    }

    @Test
    void rejectsOutOfOrderVersion() {
        InternalTokenProperties live = initial();
        InternalTokenConfigurationRefresher refresher = refresher(live);
        refresher.refresh(yaml(3, "key-3", key((byte) 3)));

        refresher.refresh(yaml(2, "key-2", key((byte) 2)));

        assertThat(live.getKeyConfigVersion()).isEqualTo(3);
        assertThat(live.getActiveKeyId()).isEqualTo("key-3");
        assertThat(refresher.currentStatus().successful()).isFalse();
    }

    private static InternalTokenConfigurationRefresher refresher(InternalTokenProperties properties) {
        NacosConfigManager manager = mock(NacosConfigManager.class);
        when(manager.getConfigService()).thenReturn(mock(ConfigService.class));
        return new InternalTokenConfigurationRefresher(
                manager, properties, "opensabre-common.yml", "DEFAULT_GROUP");
    }

    private static InternalTokenProperties initial() {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        properties.setKeyConfigVersion(1);
        properties.setActiveKeyId("key-1");
        properties.setActiveKey(key((byte) 1));
        properties.setTtl(Duration.ofSeconds(60));
        properties.setMaxTtl(Duration.ofSeconds(120));
        return properties;
    }

    private static String yaml(long version, String keyId, String key) {
        return """
                opensabre:
                  security:
                    internal-token:
                      enabled: true
                      key-config-version: %d
                      active-key-id: %s
                      active-key: %s
                      ttl: 60s
                      max-ttl: 120s
                      clock-skew: 5s
                      max-hop: 8
                """.formatted(version, keyId, key);
    }

    private static String key(byte value) {
        byte[] bytes = new byte[32];
        java.util.Arrays.fill(bytes, value);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
