package io.github.opensabre.boot.config;

import io.github.opensabre.boot.entity.SwaggerInfo;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * Swagger配置类
 */
@AutoConfiguration
@EnableConfigurationProperties(SwaggerInfo.class)
@SecuritySchemes({@SecurityScheme(
        // 指定 SecurityScheme 的名称(OpenAPIDefinition注解中的security属性中会引用该名称)
        name = "${custom.security.name}",
        // 指定认证类型为oauth2
        type = SecuritySchemeType.OAUTH2,
        // 设置认证流程
        flows = @OAuthFlows(
                // 设置授权码模式
                authorizationCode = @OAuthFlow(
                        // 获取token地址
                        tokenUrl = "${custom.security.token-url}",
                        // 授权申请地址
                        authorizationUrl = "${custom.security.authorization-url}",
                        // oauth2的申请的scope(需要在OAuth2客户端中存在)
                        scopes = {
                                @OAuthScope(name = "openid", description = "OpenId登录"),
                                @OAuthScope(name = "profile", description = "获取用户信息"),
                                @OAuthScope(name = "message.read", description = "读"),
                                @OAuthScope(name = "message.write", description = "写")
                        })
        )
)})
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
                // 外部文档
                .externalDocs(new ExternalDocumentation()
                        .description(swaggerInfo.getWikiDocumentation())
                        .url(swaggerInfo.getWikiUrl()));
    }
}
