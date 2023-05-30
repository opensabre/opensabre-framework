package io.github.opensabre.boot.config;


import io.github.opensabre.boot.event.OpensabreStartedEventHandler;
import io.github.opensabre.boot.rest.MappingInfoHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class OpensabreServiceConfig {

    @Bean
    public OpensabreStartedEventHandler opensabreStartedEventHandler() {
        return new OpensabreStartedEventHandler();
    }

    @Bean
    public MappingInfoHandler mappingInfoHandler() {
        return new MappingInfoHandler();
    }
}
