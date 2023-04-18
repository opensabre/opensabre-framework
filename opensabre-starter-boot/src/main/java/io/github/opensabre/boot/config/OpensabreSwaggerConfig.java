package io.github.opensabre.boot.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class OpensabreSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Opensabre API")
                        .description("Opensabre rest API")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("https://github.com/opensabre/opensabre-framework")))
                .externalDocs(new ExternalDocumentation()
                        .description("Opensabre Wiki Documentation")
                        .url("https://github.com/opensabre/opensabre-framework"));
    }
}
