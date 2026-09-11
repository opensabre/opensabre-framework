package io.github.opensabre.webmvc.openapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Descriptive metadata applied to the standard OpenAPI document. */
@ConfigurationProperties("opensabre.rest.swagger")
public class OpenApiDocumentProperties {

    private String version = "v0.0.1";
    private String title = "OpenSabre API";
    private String description = "OpenSabre REST API";
    private String licenseUrl = "https://github.com/opensabre/opensabre-framework";
    private String licenseName = "Apache 2.0";
    private String wikiUrl = "https://opensabre.github.io/docs";
    private String wikiDocumentation = "OpenSabre Documentation";

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLicenseUrl() { return licenseUrl; }
    public void setLicenseUrl(String licenseUrl) { this.licenseUrl = licenseUrl; }
    public String getLicenseName() { return licenseName; }
    public void setLicenseName(String licenseName) { this.licenseName = licenseName; }
    public String getWikiUrl() { return wikiUrl; }
    public void setWikiUrl(String wikiUrl) { this.wikiUrl = wikiUrl; }
    public String getWikiDocumentation() { return wikiDocumentation; }
    public void setWikiDocumentation(String wikiDocumentation) { this.wikiDocumentation = wikiDocumentation; }
}
