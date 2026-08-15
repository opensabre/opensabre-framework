# OpenSabre 1.0 升级指南

OpenSabre 1.0 以 0.7.7 为功能基线直接升级到 Spring Boot 4，不提供 Boot 3.5
过渡版本，也不提供 Jackson 2 兼容层。0.7.7 已发布的内部 Token 热刷新、刷新状态端点和
用户上下文绑定能力必须完整保留。Nacos Client 尚未支持 Jackson 3，因此允许其在内部
隔离使用 Jackson 2；OpenSabre 公共 API、业务模型和 Starter 扩展点仍统一使用 Jackson 3。

## 基线

- Java 21
- Spring Boot 4.0.7
- Spring Cloud 2025.1.2
- Spring Cloud Alibaba 2025.1.0.0
- Jackson 3（`tools.jackson`）
- Spring Security 7
- MyBatis-Plus 3.5.17
- Servlet 6.1，默认嵌入式容器为 Tomcat

## 必须处理的破坏性变更

1. 将 OpenSabre 父 POM 或 BOM 版本改为 `1.0.0-SNAPSHOT`，编译 release 改为 21。
2. 删除 `bootstrap.yml`，把应用名、端口及云配置迁入 `application.yml`。
3. 使用 `spring.config.import` 显式导入 Nacos 配置，例如：

   ```yaml
   spring:
     config:
       import:
         - optional:nacos:opensabre-common.yml?group=DEFAULT_GROUP&refreshEnabled=true
   ```

4. 业务代码将 `com.fasterxml.jackson.core`、`com.fasterxml.jackson.databind` 改为 `tools.jackson.core`、`tools.jackson.databind`。Jackson 注解仍使用 `com.fasterxml.jackson.annotation`。
5. MyBatis-Plus 服务接口及实现由 `com.baomidou.mybatisplus.extension.service` 改为 `com.baomidou.mybatisplus.spring.service`。
6. Spring Security 7 中授权服务器过滤器链必须通过授权端点 matcher 限定范围；JWT 认证还会附加认证因子 authority，测试不要假设 authority 集合只有业务角色。
7. Boot 4 测试用 `@MockitoBean` 替换已移除的 `@MockBean`。
8. WebMVC starter 不再传递 Springdoc/Knife4j。若业务必须暴露文档，需单独选择并验证原生 Jackson 3 兼容方案。

## 验证门槛

- 框架全模块执行 `mvn test`。
- 每个基础服务执行 `mvn test`，至少覆盖完整 ApplicationContext 启动。
- 消费样例执行 `mvn test`，验证 starter、Config Data 与默认 Tomcat。
- 使用依赖树确认 `com.fasterxml.jackson.core:jackson-databind` 仅来自 Nacos Client，
  OpenSabre 自身不得直接依赖或向公共 API 暴露 Jackson 2 databind/core 类型。
