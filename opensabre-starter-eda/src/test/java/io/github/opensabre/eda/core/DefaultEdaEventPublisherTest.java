package io.github.opensabre.eda.core;

import io.github.opensabre.eda.api.EdaEvent;
import io.github.opensabre.eda.api.EdaEventHandler;
import io.github.opensabre.eda.api.EventTarget;
import io.github.opensabre.eda.api.EventTransport;
import io.github.opensabre.eda.config.EdaProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultEdaEventPublisherTest {

    private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void shouldDispatchOnlyMatchingLocalHandlerAsynchronously() throws Exception {
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger handled = new AtomicInteger();
        EdaEventHandler<String> matching = new EdaEventHandler<>() {
            @Override
            public String eventType() {
                return "demo.created";
            }

            @Override
            public void handle(EdaEvent<String> event) {
                handled.incrementAndGet();
                latch.countDown();
            }
        };
        EdaEventHandler<String> ignored = new EdaEventHandler<>() {
            @Override
            public String eventType() {
                return "demo.deleted";
            }

            @Override
            public void handle(EdaEvent<String> event) {
                handled.addAndGet(100);
            }
        };

        DefaultEdaEventPublisher publisher = publisher(List.of(matching, ignored), List.of());
        publisher.publishLocal(EdaEvent.of("demo.created", "producer", "payload"));

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(handled).hasValue(1);
    }

    @Test
    void shouldPublishToRemoteTransport() {
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();
        AtomicInteger published = new AtomicInteger();
        EventTransport transport = event -> published.incrementAndGet();
        DefaultEdaEventPublisher publisher = publisher(List.of(), List.of(transport));

        publisher.publish(EdaEvent.of("demo.created", "producer", "payload"), EventTarget.REMOTE);

        assertThat(published).hasValue(1);
    }

    @Test
    void shouldContinueDispatchingWhenOneLocalHandlerFails() throws Exception {
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.initialize();
        CountDownLatch latch = new CountDownLatch(1);
        EdaEventHandler<String> failing = new EdaEventHandler<>() {
            @Override
            public String eventType() {
                return "demo.created";
            }

            @Override
            public void handle(EdaEvent<String> event) {
                throw new IllegalStateException("handler failure");
            }
        };
        EdaEventHandler<String> succeeding = new EdaEventHandler<>() {
            @Override
            public String eventType() {
                return "demo.created";
            }

            @Override
            public void handle(EdaEvent<String> event) {
                latch.countDown();
            }
        };

        publisher(List.of(failing, succeeding), List.of())
                .publishLocal(EdaEvent.of("demo.created", "producer", "payload"));

        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void shouldFailRemotePublishWhenTransportIsRequired() {
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();
        EdaProperties properties = new EdaProperties();
        properties.getPublisher().setRemoteRequired(true);
        DefaultEdaEventPublisher publisher = new DefaultEdaEventPublisher(provider(List.of()), provider(List.of()),
                executor, properties, provider(List.of()));

        assertThatThrownBy(() -> publisher.publish(EdaEvent.of("demo.created", "producer", "payload"),
                EventTarget.REMOTE)).isInstanceOf(IllegalStateException.class)
                .hasMessage("No EDA remote transport is available");
    }

    private DefaultEdaEventPublisher publisher(List<EdaEventHandler<?>> handlers, List<EventTransport> transports) {
        return new DefaultEdaEventPublisher(provider(handlers), provider(transports), executor,
                new EdaProperties(), provider(List.of()));
    }

    private static <T> ObjectProvider<T> provider(List<T> values) {
        return new ObjectProvider<>() {
            @Override
            public T getObject() {
                if (values.isEmpty()) {
                    throw new NoSuchElementException("No object available");
                }
                return values.get(0);
            }

            @Override
            public T getObject(Object... args) {
                return getObject();
            }

            @Override
            public T getIfAvailable() {
                return values.isEmpty() ? null : values.get(0);
            }

            @Override
            public T getIfUnique() {
                return values.size() == 1 ? values.get(0) : null;
            }

            @Override
            public Iterator<T> iterator() {
                return values.iterator();
            }

            @Override
            public Stream<T> orderedStream() {
                return values.stream();
            }
        };
    }
}
