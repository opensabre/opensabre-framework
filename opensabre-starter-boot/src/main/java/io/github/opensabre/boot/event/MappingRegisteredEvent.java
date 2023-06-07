package io.github.opensabre.boot.event;

import org.springframework.context.ApplicationEvent;

/**
 * 自定义Spring事件，用于Rest信息以事件方式发送出去
 */
public class MappingRegisteredEvent extends ApplicationEvent {
    public MappingRegisteredEvent(Object source) {
        super(source);
    }
}