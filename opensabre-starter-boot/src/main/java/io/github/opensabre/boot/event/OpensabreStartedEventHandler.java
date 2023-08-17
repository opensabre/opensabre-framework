package io.github.opensabre.boot.event;

import io.github.opensabre.boot.rest.MappingInfoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

import javax.annotation.Resource;

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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("ApplicationReadyEvent received");
        mappingInfoHandler.getMappingInfo().forEach(mappingInfo -> {
            context.publishEvent(new MappingRegisteredEvent(mappingInfo));
            log.info("Mapping Registered :{}", mappingInfo);
        });
    }
}