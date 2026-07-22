package io.github.opensabre.boot.event;

import io.github.opensabre.boot.entity.ResourceMappingSnapshot;
import io.github.opensabre.boot.entity.RestMappingInfo;
import io.github.opensabre.boot.rest.MappingInfoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import jakarta.annotation.Resource;
import java.util.Set;

/**
 * springboot应用启动完成后，发送Rest注册事件
 */
@Slf4j
public class OpensabreStartedEventHandler implements ApplicationListener<ApplicationReadyEvent> {
    /**
     * spring上下文
     */
    @Resource
    private ApplicationContext context;
    /**
     * Rest信息获取处对象
     */
    @Resource
    MappingInfoHandler mappingInfoHandler;

    @Resource
    Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("ApplicationReadyEvent received");
        Set<RestMappingInfo> mappings = mappingInfoHandler.getMappingInfo();
        mappings.forEach(mappingInfo -> {
            context.publishEvent(new MappingRegisteredEvent(mappingInfo));
            log.info("Mapping Registered :{}", mappingInfo);
        });
        ResourceMappingSnapshot snapshot = ResourceMappingSnapshot.builder()
                .application(environment.getProperty("spring.application.name", context.getApplicationName()))
                .version(environment.getProperty("info.app.version", "unknown"))
                .resources(mappings)
                .build();
        context.publishEvent(new ResourceMappingsRegisteredEvent(snapshot));
        log.info("Resource mapping snapshot registered: application={}, count={}",
                snapshot.getApplication(), mappings.size());
    }
}
