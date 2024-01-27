package io.github.opensabre.boot.config;

import io.github.opensabre.boot.entity.SwaggerInfo;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * Swagger配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(SwaggerInfo.class)
public class OpensabreSwaggerConfig {

    @Resource
    private SwaggerInfo swaggerInfo;

    /**
     * Swagger配置对象初使化
     *
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title(swaggerInfo.getTitle())
                        .description(swaggerInfo.getDescription())
                        .version(swaggerInfo.getVersion())
                        .license(new License().name(swaggerInfo.getLicenseName()).url(swaggerInfo.getLicenseUrl())))
                .externalDocs(new ExternalDocumentation()
                        .description(swaggerInfo.getWikiDocumentation())
                        .url(swaggerInfo.getWikiUrl()));
    }
}
