package io.github.opensabre.rpc.openfeign.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreFeignClientConfigurationTest {

    @Test
    void waitsForConverterInitializationWhenFirstRequestsAreConcurrent() throws Exception {
        CountDownLatch customizerEntered = new CountDownLatch(1);
        CountDownLatch continueInitialization = new CountDownLatch(1);
        HttpMessageConverterCustomizer blockingCustomizer = converters -> {
            customizerEntered.countDown();
            try {
                continueInitialization.await();
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        };

        OpensabreFeignClientConfiguration configuration = new OpensabreFeignClientConfiguration();
        FeignHttpMessageConverters converters = configuration.opensabreFeignHttpMessageConverters(
                emptyClientCustomizerProvider(), providerOf(blockingCustomizer));

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<List<?>> first = executor.submit(converters::getConverters);
            customizerEntered.await();
            Future<List<?>> second = executor.submit(converters::getConverters);

            Thread.sleep(100);
            assertThat(second).isNotDone();

            continueInitialization.countDown();
            assertThat(first.get()).isNotEmpty();
            assertThat(second.get()).isNotEmpty();
        }
    }

    private static ObjectProvider<ClientHttpMessageConvertersCustomizer>
            emptyClientCustomizerProvider() {
        return new StaticListableBeanFactory()
                .getBeanProvider(ClientHttpMessageConvertersCustomizer.class);
    }

    private static ObjectProvider<HttpMessageConverterCustomizer> providerOf(
            HttpMessageConverterCustomizer... beans) {
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        for (int index = 0; index < beans.length; index++) {
            beanFactory.addBean("bean" + index, beans[index]);
        }
        return beanFactory.getBeanProvider(HttpMessageConverterCustomizer.class);
    }
}
