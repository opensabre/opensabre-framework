package io.github.opensabre.eda.core;

import io.github.opensabre.eda.api.EdaEvent;
import io.github.opensabre.eda.api.EdaEventHandler;
import io.github.opensabre.eda.api.EdaEventPublisher;
import io.github.opensabre.eda.api.EventTarget;
import io.github.opensabre.eda.api.EventTransport;
import io.github.opensabre.eda.config.EdaProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

/**
 * 默认事件发布器，负责本地异步分发和远程传输适配器调用。
 */
@Slf4j
public class DefaultEdaEventPublisher implements EdaEventPublisher {

    private final ObjectProvider<EdaEventHandler<?>> handlersProvider;
    private final ObjectProvider<EventTransport> transportsProvider;
    private final ThreadPoolTaskExecutor executor;
    private final EdaProperties properties;
    private final MeterRegistry meterRegistry;

    public DefaultEdaEventPublisher(ObjectProvider<EdaEventHandler<?>> handlersProvider,
                                    ObjectProvider<EventTransport> transportsProvider,
                                    ThreadPoolTaskExecutor executor, EdaProperties properties,
                                    ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.handlersProvider = handlersProvider;
        this.transportsProvider = transportsProvider;
        this.executor = executor;
        this.properties = properties;
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
    }

    @Override
    public void publish(EdaEvent<?> event, EventTarget... targets) {
        Objects.requireNonNull(event, "event must not be null");
        Set<EventTarget> resolvedTargets = targets == null || targets.length == 0
                ? EnumSet.of(EventTarget.LOCAL) : EnumSet.copyOf(Arrays.asList(targets));
        if (resolvedTargets.contains(EventTarget.LOCAL)) {
            publishLocalEvent(event);
        }
        if (resolvedTargets.contains(EventTarget.REMOTE)) {
            publishRemoteEvent(event);
        }
    }

    @Override
    public void publishAfterCommit(EdaEvent<?> event, EventTarget... targets) {
        Objects.requireNonNull(event, "event must not be null");
        EventTarget[] capturedTargets = targets == null ? new EventTarget[0] : targets.clone();
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            publish(event, capturedTargets);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event, capturedTargets);
            }
        });
    }

    private void publishLocalEvent(EdaEvent<?> event) {
        List<EdaEventHandler<?>> matchingHandlers = handlersProvider.orderedStream()
                .filter(handler -> event.eventType().equals(handler.eventType()))
                .toList();
        if (matchingHandlers.isEmpty()) {
            count("opensabre.eda.events.unhandled", event, "local", null);
            log.debug("No local EDA handler registered: eventId={}, eventType={}", event.eventId(), event.eventType());
            return;
        }
        count("opensabre.eda.events.published", event, "local", null);
        for (EdaEventHandler<?> handler : matchingHandlers) {
            try {
                executor.execute(() -> invokeHandler(handler, event));
            } catch (RejectedExecutionException exception) {
                count("opensabre.eda.events.rejected", event, "local", handler);
                log.warn("Discard local EDA event because executor is saturated: eventId={}, eventType={}, handler={}",
                        event.eventId(), event.eventType(), handler.getClass().getSimpleName());
            }
        }
    }

    private void publishRemoteEvent(EdaEvent<?> event) {
        List<EventTransport> transports = transportsProvider.orderedStream().toList();
        if (transports.isEmpty()) {
            count("opensabre.eda.events.transport.failed", event, "remote", null);
            if (properties.getPublisher().isRemoteRequired()) {
                throw new IllegalStateException("No EDA remote transport is available");
            }
            log.warn("Skip remote EDA event because no transport is available: eventId={}, eventType={}",
                    event.eventId(), event.eventType());
            return;
        }
        for (EventTransport transport : transports) {
            try {
                transport.publish(event);
                count("opensabre.eda.events.published", event, "remote", transport);
            } catch (RuntimeException exception) {
                count("opensabre.eda.events.transport.failed", event, "remote", transport);
                if (properties.getPublisher().isRemoteRequired()) {
                    throw exception;
                }
                log.warn("Failed to publish remote EDA event: eventId={}, eventType={}, transport={}",
                        event.eventId(), event.eventType(), transport.getClass().getSimpleName(), exception);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void invokeHandler(EdaEventHandler handler, EdaEvent<?> event) {
        String handlerName = handler.getClass().getSimpleName();
        try {
            handler.handle(event);
            count("opensabre.eda.events.handled", event, "local", handler);
        } catch (Exception exception) {
            count("opensabre.eda.events.failed", event, "local", handler);
            log.warn("EDA event handler failed: eventId={}, eventType={}, handler={}",
                    event.eventId(), event.eventType(), handlerName, exception);
        }
    }

    private void count(String name, EdaEvent<?> event, String target, Object consumer) {
        if (meterRegistry == null) {
            return;
        }
        String consumerName = consumer == null ? "none" : consumer.getClass().getSimpleName();
        Counter.builder(name).tag("event_type", event.eventType()).tag("target", target)
                .tag("consumer", consumerName).register(meterRegistry).increment();
    }
}
