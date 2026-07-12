WEB通用公共包
----------

## 简介

主要封装 Web 开发中与 Spring MVC、Spring WebFlux 无关的通用公共类、工具类，如统一返回模型、通用异常定义、基础表单/VO、校验注解和用户上下文等。

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
    <artifactId>opensabre-web</artifactId>
    <version>0.3.0</version>
</dependency>
```
