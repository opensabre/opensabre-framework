WEBFLUX公共包
----------

## 简介

主要封装 Spring WebFlux Web 开发用到的通用公共能力，帮助开发者快速构建 reactive Web 应用。

当前模块提供：

- Spring Boot 自动装配。
- WebFlux 全局异常处理。
- Opensabre 统一 `Result` 响应模型。

## 使用

进入应用目录。

安装命令：`mvn install`

## 使用指南

### 应用引入

pom.xml

```xml
<dependency>
    <groupId>io.github.opensabre</groupId>
    <artifactId>opensabre-starter-webflux</artifactId>
    <version>1.1.4</version>
</dependency>
```

## 网关 OpenAPI 聚合

当 classpath 中存在 Spring Cloud Gateway 时，starter 从当前 `RouteDefinitionLocator` 自动生成
Knife4j 文档服务清单，并在 `RefreshRoutesEvent` 后重建清单，不需要在应用配置中维护
`springdoc.swagger-ui.urls`。仅聚合 `lb://` 且具有静态 `Path` 前缀的路由；同一服务存在多条路由时
优先选择 `/api/` 前缀。

控制面发布路由时可通过 metadata 显式声明文档入口。显式入口优先于路径推断；设置
`opensabre.openapi.enabled=false` 可排除不应展示文档的内部服务。路径推断兼容 Gateway 简写生成的
`_genkey_N`、单值 `pattern`，以及控制面序列化的 `patterns.N` 参数格式。

```yaml
metadata:
  opensabre.openapi.enabled: true
  opensabre.openapi.path: /api/iqc/v3/api-docs
  opensabre.openapi.name: IQC 平台
```

```yaml
opensabre:
  openapi:
    gateway:
      enabled: true
      api-docs-path: /v3/api-docs
      excluded-route-ids:
        - internal-only
      display-names:
        base-authorization: 授权服务
```

关闭 `opensabre.openapi.gateway.enabled` 可停用聚合；页面和接口是否允许访问仍由网关安全策略决定。
