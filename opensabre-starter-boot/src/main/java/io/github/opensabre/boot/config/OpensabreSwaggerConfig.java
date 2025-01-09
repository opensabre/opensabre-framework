package io.github.opensabre.boot.config;

import io.github.opensabre.boot.entity.SwaggerInfo;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;

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
                .info(new Info()
                        .title(swaggerInfo.getTitle())
                        .description(swaggerInfo.getDescription())
                        .version(swaggerInfo.getVersion())
                        .license(new License().name(swaggerInfo.getLicenseName()).url(swaggerInfo.getLicenseUrl())))
                // 配置全局鉴权参数-Authorize
                .components(new Components()
                        .addSecuritySchemes(HttpHeaders.AUTHORIZATION,
                                new SecurityScheme()
                                        .name(HttpHeaders.AUTHORIZATION)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .scheme("Bearer")
                                        .bearerFormat("JWT")))
                // 外部文档
                .externalDocs(new ExternalDocumentation()
                        .description(swaggerInfo.getWikiDocumentation())
                        .url(swaggerInfo.getWikiUrl()));
    }
}
