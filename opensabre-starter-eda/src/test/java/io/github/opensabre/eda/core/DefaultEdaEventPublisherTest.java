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
