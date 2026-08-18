# OpenSabre 1.1 升级指南

Framework 1.1 将 Spring Boot 从 4.0.7 升级到 4.1.0，保持 Java 21 为编译和最低运行基线。
JDK 21 与 JDK 25 是 CI 保障版本；高于 JDK 25 的运行兼容性仅在不依赖新增 JDK API 的前提下提供
best-effort 支持。

## 版本基线

- Java：编译 `--release 21`；验证 JDK 21 和 JDK 25。
- Spring Boot：4.1.0。
- Spring Cloud：2025.1.2。
- Spring Cloud Alibaba：2025.1.0.0（已发布版本）。

Spring Cloud 2025.1.2 已支持 Spring Boot 4.1。Spring Cloud Alibaba 的已发布 2025.1.0.0
文档仍只声明 Boot 4.0.x；因此其 Nacos、Sentinel、RocketMQ 或 Seata 使用者必须完成自己的集成
回归。Framework 1.1 Release 前还必须锁定其正式的 Boot 4.1 兼容版本，或取得维护者批准的例外；
不得以 SNAPSHOT 作为发布依赖。

## 应用升级步骤

1. 将父 POM 或 BOM 更新至 Framework 1.1 候选版本，保留 `maven.compiler.release` 为 `21`。
2. 在 JDK 21、JDK 25 分别执行应用的 `mvn test`；业务使用 Spring Cloud Alibaba 时，至少启动一次
   使用真实 Nacos Config Data 的集成环境。
3. 验证配置实际订阅预期 Nacos Data ID、服务注册、数据库、缓存、RPC、鉴权和健康检查。
4. 若应用使用反射、JDK 动态代理、资源扫描或动态序列化，在 Native 阶段将所需提示提交到对应
   Framework Starter，而不是在每个应用复制 `reflect-config.json`。

## Nacos 公共配置

`opensabre-common.yml` 是首次启动可选的公共配置，统一使用以下导入形式：

```yaml
spring:
  config:
    import: optional:nacos:${OPENSABRE_COMMON_CONFIG_DATA_ID:opensabre-common.yml}?group=${OPENSABRE_COMMON_CONFIG_GROUP:DEFAULT_GROUP}&refreshEnabled=true
```

Data ID 默认是 `.yml`（不是 `.yaml`）；可通过 `OPENSABRE_COMMON_CONFIG_DATA_ID` 和
`OPENSABRE_COMMON_CONFIG_GROUP` 覆盖。Nacos 暂不可用、Data ID 不存在或内容为空不能阻断未启用
内部 Token 的应用首次启动。若显式设置 `opensabre.security.internal-token.enabled=true`，必须在
上线前发布并校验有效公共配置；框架不会生成或回退到固定共享密钥。

## 构建与发布门禁

Framework 使用：

```bash
mvn -B verify javadoc:javadoc --file pom.xml
```

GitHub Actions 对 JDK 21 和 JDK 25 执行相同命令。发布前还必须完成受影响 base 应用的真实运行
验证；CI 通过不替代 Nacos、鉴权和网关等基础设施链路验证。

## 已知限制

- 此阶段不改变默认 JVM 打包路径；Native Image 和双模式 OCI 镜像将作为独立阶段交付。
- Spring Cloud Alibaba 的正式 Boot 4.1 认证尚待其上游发布，不能据此直接宣称所有 Alibaba 组件已
  获完整生产认证。
