package io.github.opensabre.boot.event;

import io.github.opensabre.boot.rest.MappingInfoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class OpensabreStartedEventHandler implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ApplicationContext context;

    @Resource
    MappingInfoHandler mappingInfoHandler;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("ApplicationReadyEvent received");
        mappingInfoHandler.getMappingInfo().forEach(mappingInfo -> {
            context.publishEvent(new MappingRegisteredEvent(mappingInfo));
            log.info("Mapping Registered :{}", mappingInfo);
        });
    }
}