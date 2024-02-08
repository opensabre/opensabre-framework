package io.github.opensabre.boot.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "opensabre.rest.swagger")
public class SwaggerInfo {
    private String version;
    private String title;
    private String description;
    private String licenseUrl;
    private String licenseName;
    private String wikiUrl;
    private String wikiDocumentation;
}
