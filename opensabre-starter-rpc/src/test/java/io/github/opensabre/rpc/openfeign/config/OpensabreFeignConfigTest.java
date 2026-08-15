package io.github.opensabre.rpc.openfeign.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreFeignConfigTest {

    private final OpensabreFeignConfig config = new OpensabreFeignConfig();

    @Test
    void addsJackson3ConverterWhenFeignDefaultsDoNotContainOne() {
        HttpMessageConverterCustomizer customizer =
                config.opensabreFeignJackson3HttpMessageConverter(JsonMapper.builder().build());
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        customizer.accept(converters);

        assertThat(converters).singleElement().isInstanceOf(JacksonJsonHttpMessageConverter.class);
    }

    @Test
    void doesNotDuplicateExistingJackson3Converter() {
        JsonMapper jsonMapper = JsonMapper.builder().build();
        HttpMessageConverterCustomizer customizer =
                config.opensabreFeignJackson3HttpMessageConverter(jsonMapper);
        List<HttpMessageConverter<?>> converters =
                new ArrayList<>(List.of(new JacksonJsonHttpMessageConverter(jsonMapper)));

        customizer.accept(converters);

        assertThat(converters).hasSize(1);
    }
}
