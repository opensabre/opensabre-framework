package io.github.opensabre.boot.config;


import io.github.opensabre.boot.event.OpensabreStartedEventHandler;
import io.github.opensabre.boot.rest.MappingInfoHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({OpensabreStartedEventHandler.class, MappingInfoHandler.class})
public class OpensabreServiceConfig {
}
