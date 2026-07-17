package io.github.opensabre.boot.config;


import io.github.opensabre.boot.event.OpensabreStartedEventHandler;
import io.github.opensabre.boot.rest.MappingInfoHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * Opensabre Rest信息事件通知配置类
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({OpensabreStartedEventHandler.class, MappingInfoHandler.class})
public class OpensabreServiceConfig {
}
