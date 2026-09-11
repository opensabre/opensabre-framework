WEBMVC公共包
----------

## 简介

主要封装 Spring MVC Web 开发用到的 Servlet/MVC 特性能力，如 MVC 自动装配、MVC 全局异常处理、MVC 统一响应包装、Servlet 用户上下文拦截器等。

## 使用

进入应用目录

安装命令：`mvn install`

## 使用指南

### 应用引入

需要将编译生成的jar包安装到本地maven类进入引用使用。

pom.xml

```
<dependency>
    <groupId>io.github.opensabre</groupId>
    <artifactId>opensabre-starter-webmvc</artifactId>
    <version>1.1.4</version>
</dependency>
```

## OpenAPI 与 Knife4j

1.1.4 起 starter 提供与当前 Spring Boot/Jackson 版本兼容的 Springdoc 和 Knife4j，默认暴露
`/v3/api-docs`、`/v3/api-docs/swagger-config` 与 `/doc.html`。应用只需通过
`opensabre.rest.swagger` 设置标题、描述、版本和许可证；应用自定义 `OpenAPI` Bean 时，
starter 会自动退让。

使用 Springdoc 标准开关统一控制暴露：

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

生产环境是否允许访问这些路径仍由应用或网关的 Spring Security 策略决定，starter 不匿名放行。

## 依赖边界

Servlet Web 应用应同时显式引入 `opensabre-starter-boot` 和本 starter。本 starter 使用 Boot 4
默认 Tomcat。WebFlux 或非 Web 服务只引入所需 starter。
