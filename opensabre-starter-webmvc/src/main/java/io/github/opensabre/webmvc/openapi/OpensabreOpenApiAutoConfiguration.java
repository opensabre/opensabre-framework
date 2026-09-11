package io.github.opensabre.webmvc.openapi;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Supplies the standard OpenSabre OpenAPI document metadata for servlet applications. */
@AutoConfiguration(after = io.github.opensabre.webmvc.config.OpensabreWebMvcConfig.class)
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = "enabled",
        havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OpenApiDocumentProperties.class)
public class OpensabreOpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI opensabreOpenApi(OpenApiDocumentProperties properties) {
        return new OpenAPI()
                .info(new Info()
                        .version(properties.getVersion())
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .license(new License()
                                .name(properties.getLicenseName())
                                .url(properties.getLicenseUrl())))
                .externalDocs(new ExternalDocumentation()
                        .description(properties.getWikiDocumentation())
                        .url(properties.getWikiUrl()));
    }
}
