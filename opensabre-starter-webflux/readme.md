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
    <version>0.3.0</version>
</dependency>
```
