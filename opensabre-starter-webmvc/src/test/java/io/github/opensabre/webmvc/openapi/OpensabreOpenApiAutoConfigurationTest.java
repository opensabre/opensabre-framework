package io.github.opensabre.webmvc.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OpensabreOpenApiAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpensabreOpenApiAutoConfiguration.class));

    @Test
    void suppliesConfiguredDocumentMetadata() {
        contextRunner.withPropertyValues(
                "opensabre.rest.swagger.title=Inventory API",
                "opensabre.rest.swagger.version=1.2.3")
                .run(context -> {
                    OpenAPI document = context.getBean(OpenAPI.class);
                    assertThat(document.getInfo().getTitle()).isEqualTo("Inventory API");
                    assertThat(document.getInfo().getVersion()).isEqualTo("1.2.3");
                });
    }

    @Test
    void backsOffForApplicationOwnedDocument() {
        contextRunner.withBean(OpenAPI.class, OpenAPI::new)
                .run(context -> assertThat(context).hasSingleBean(OpenAPI.class));
    }

    @Test
    void canDisableApiDocuments() {
        contextRunner.withPropertyValues("springdoc.api-docs.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(OpenAPI.class));
    }
}
