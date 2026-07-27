package io.github.opensabre.boot.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Complete MVC resource snapshot emitted by one application instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMappingSnapshot {
    private String application;
    private String version;
    private Set<RestMappingInfo> resources;
}
