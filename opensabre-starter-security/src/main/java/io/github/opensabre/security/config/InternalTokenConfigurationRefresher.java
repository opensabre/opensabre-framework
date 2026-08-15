package io.github.opensabre.security.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import io.github.opensabre.security.token.HmacKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/** Subscribes to the common Nacos document and atomically refreshes internal-token settings. */
public class InternalTokenConfigurationRefresher implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(InternalTokenConfigurationRefresher.class);
    private static final String PREFIX = "opensabre.security.internal-token";

    private final ConfigService configService;
    private final InternalTokenProperties properties;
    private final String dataId;
    private final String group;
    private final AtomicReference<InternalTokenRefreshStatus> status;
    private final Listener listener = new Listener() {
        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            refresh(configInfo);
        }
    };
    private volatile boolean running;

    public InternalTokenConfigurationRefresher(
            NacosConfigManager configManager,
            InternalTokenProperties properties,
            String dataId,
            String group) {
        this.configService = configManager.getConfigService();
        this.properties = properties;
        this.dataId = dataId;
        this.group = group;
        InternalTokenProperties initial = properties.snapshot();
        this.status = new AtomicReference<>(new InternalTokenRefreshStatus(
                initial.getKeyConfigVersion(), initial.getActiveKeyId(), Instant.now(), true, "initial"));
    }

    @Override
    public void start() {
        try {
            configService.addListener(dataId, group, listener);
            running = true;
            log.info("Subscribed to internal-token configuration {}/{}", group, dataId);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot subscribe to internal-token configuration", exception);
        }
    }

    @Override
    public void stop() {
        configService.removeListener(dataId, group, listener);
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public InternalTokenRefreshStatus currentStatus() {
        return status.get();
    }

    void refresh(String yaml) {
        InternalTokenProperties before = properties.snapshot();
        try {
            InternalTokenProperties candidate = bind(yaml);
            validate(candidate);
            if (candidate.getKeyConfigVersion() < before.getKeyConfigVersion()) {
                throw new IllegalArgumentException("key-config-version must not decrease");
            }
            if (candidate.getKeyConfigVersion() == before.getKeyConfigVersion()) {
                return;
            }
            properties.replaceWith(candidate);
            Instant now = Instant.now();
            status.set(new InternalTokenRefreshStatus(
                    candidate.getKeyConfigVersion(), candidate.getActiveKeyId(), now, true, "refreshed"));
            log.info("Refreshed internal-token configuration: version={}, activeKeyId={}",
                    candidate.getKeyConfigVersion(), candidate.getActiveKeyId());
        } catch (Exception exception) {
            status.set(new InternalTokenRefreshStatus(
                    before.getKeyConfigVersion(), before.getActiveKeyId(), Instant.now(), false,
                    exception.getMessage()));
            log.error("Rejected internal-token configuration refresh; keeping version {}: {}",
                    before.getKeyConfigVersion(), exception.getMessage());
        }
    }

    private static InternalTokenProperties bind(String yaml) throws Exception {
        MutablePropertySources sources = new MutablePropertySources();
        for (PropertySource<?> source : new YamlPropertySourceLoader().load(
                "opensabreInternalTokenRefresh",
                new ByteArrayResource(yaml.getBytes(StandardCharsets.UTF_8)))) {
            sources.addLast(source);
        }
        return new Binder(ConfigurationPropertySources.from(sources))
                .bind(PREFIX, Bindable.of(InternalTokenProperties.class))
                .orElseThrow(() -> new IllegalArgumentException("internal-token configuration is missing"));
    }

    private static void validate(InternalTokenProperties candidate) {
        if (!candidate.isEnabled()) {
            return;
        }
        new HmacKeyRing(candidate);
        long ttl = candidate.getTtl().toSeconds();
        long maxTtl = candidate.getMaxTtl().toSeconds();
        if (ttl <= 0 || maxTtl <= 0 || ttl > maxTtl || maxTtl > 120) {
            throw new IllegalArgumentException("ttl/max-ttl configuration is invalid");
        }
        if (candidate.getClockSkew().isNegative() || candidate.getMaxHop() <= 0) {
            throw new IllegalArgumentException("clock-skew/max-hop configuration is invalid");
        }
    }
}
