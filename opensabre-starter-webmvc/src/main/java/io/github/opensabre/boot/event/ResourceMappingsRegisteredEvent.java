package io.github.opensabre.boot.event;

import io.github.opensabre.boot.entity.ResourceMappingSnapshot;
import org.springframework.context.ApplicationEvent;

/**
 * Published once after the complete MVC resource snapshot has been collected.
 */
public class ResourceMappingsRegisteredEvent extends ApplicationEvent {
    public ResourceMappingsRegisteredEvent(ResourceMappingSnapshot source) {
        super(source);
    }

    public ResourceMappingSnapshot getSnapshot() {
        return (ResourceMappingSnapshot) getSource();
    }
}
