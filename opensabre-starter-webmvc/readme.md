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
    <version>0.3.0</version>
</dependency>
```
