package io.github.opensabre.security.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalTokenConfigurationRefresherTest {
    @Test
    void acceptsHigherValidVersion() {
        InternalTokenProperties properties = properties(1, "key-1");
        InternalTokenConfigurationRefresher refresher = refresher(properties);
        refresher.refresh(yaml(2, "key-2", key(2)));
        assertThat(properties.getKeyConfigVersion()).isEqualTo(2);
        assertThat(properties.getActiveKeyId()).isEqualTo("key-2");
        assertThat(refresher.currentStatus().successful()).isTrue();
    }

    @Test
    void rejectsInvalidKeyAndKeepsPreviousSnapshot() {
        InternalTokenProperties properties = properties(1, "key-1");
        InternalTokenConfigurationRefresher refresher = refresher(properties);
        refresher.refresh(yaml(2, "key-2", Base64.getEncoder().encodeToString(new byte[8])));
        assertThat(properties.getKeyConfigVersion()).isEqualTo(1);
        assertThat(refresher.currentStatus().successful()).isFalse();
    }

    @Test
    void rejectsOutOfOrderVersion() {
        InternalTokenProperties properties = properties(3, "key-3");
        InternalTokenConfigurationRefresher refresher = refresher(properties);
        refresher.refresh(yaml(2, "key-2", key(2)));
        assertThat(properties.getKeyConfigVersion()).isEqualTo(3);
        assertThat(refresher.currentStatus().successful()).isFalse();
    }

    private static InternalTokenConfigurationRefresher refresher(InternalTokenProperties properties) {
        NacosConfigManager manager = mock(NacosConfigManager.class);
        when(manager.getConfigService()).thenReturn(mock(ConfigService.class));
        return new InternalTokenConfigurationRefresher(manager, properties, "opensabre-common.yml", "DEFAULT_GROUP");
    }

    private static InternalTokenProperties properties(long version, String id) {
        InternalTokenProperties properties = new InternalTokenProperties();
        properties.setEnabled(true);
        properties.setKeyConfigVersion(version);
        properties.setActiveKeyId(id);
        properties.setActiveKey(key((int) version));
        properties.setTtl(Duration.ofSeconds(30));
        properties.setMaxTtl(Duration.ofSeconds(60));
        properties.setAllowedIssuers(Set.of("base-sysadmin"));
        return properties;
    }

    private static String yaml(long version, String id, String encodedKey) {
        return "opensabre:\n  security:\n    internal-token:\n      enabled: true\n"
                + "      key-config-version: " + version + "\n      active-key-id: " + id + "\n"
                + "      active-key: " + encodedKey + "\n      ttl: 30s\n      max-ttl: 60s\n"
                + "      clock-skew: 5s\n      max-hop: 3\n      allowed-issuers: [base-sysadmin]\n";
    }

    private static String key(int seed) {
        byte[] value = new byte[32];
        java.util.Arrays.fill(value, (byte) seed);
        return Base64.getEncoder().encodeToString(value);
    }
}
