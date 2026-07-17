WEBMVC公共包
----------

## 简介

主要封装 Spring MVC Web 开发用到的 Servlet/MVC 特性能力，如 MVC 自动装配、MVC 全局异常处理、MVC 统一响应包装、Servlet 用户上下文拦截器、OpenAPI 与 Knife4j 文档界面等。

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
    <version>0.3.0</version>
</dependency>
```

## 0.5.0 依赖调整

从 0.5.0 开始，`opensabre-starter-boot` 不再传递引入 Servlet、Springdoc 或 Knife4j。Servlet Web 应用应同时显式引入 `opensabre-starter-boot` 和本 starter；本 starter 会提供原有的 `opensabre.rest.swagger.*` 配置、OpenAPI Bean、Knife4j UI 及 REST 映射通知能力。

WebFlux 或非 Web 服务只引入所需的 Boot、WebFlux 等 starter，不应引入本 starter。
