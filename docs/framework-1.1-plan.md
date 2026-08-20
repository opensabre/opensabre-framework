# Framework 1.1 升级实施计划

关联 Issue：[opensabre-framework#64](https://github.com/opensabre/opensabre-framework/issues/64)。本计划以
Java 21 为编译和最低运行基线，以 JDK 21、25 为 CI 保障版本；JDK 25 以上仅承诺不使用高版本
编译期 API 前提下的 best-effort 运行兼容。

## 基线与约束

| 项目 | 1.0.0 基线 | 1.1 目标 | 门禁 |
| --- | --- | --- | --- |
| Spring Boot | 4.0.7 | 4.1.0 | 所有 Starter 的 `mvn -B verify` 通过 |
| Spring Cloud | 2025.1.2 | 2025.1.2 | 已官方声明支持 Boot 4.1 |
| Spring Cloud Alibaba | 2025.1.0.0 | 待正式兼容版本 | 不使用上游 SNAPSHOT 发布 |
| Java | 21 | 编译 21；CI 21/25 | 两套完整构建、测试和 Javadoc 通过 |
| 镜像 | Temurin 21 Alpine JVM | Temurin 25 Alpine JVM | 非 root、日志、时区、健康检查及容器内存参数均验证 |

Spring Cloud Alibaba 的公开 2025.1.0.0 版本表只声明支持 Boot 4.0.x；其 `2025.1.x` 分支虽已
切换到 Boot 4.1.0 和 Cloud 2025.1.2，但尚未发布为正式 BOM。Framework 可在保持已发布
`2025.1.0.0` 的前提下进行编译与回归验证，但不能据此宣称完整生产认证；1.1 Release 前必须锁定
上游正式兼容版本或取得维护者明确的例外批准，且不得引入 SNAPSHOT。

## 实施顺序

### 阶段 1：构建矩阵与可复现基线（进行中）

1. 1.1.0 已作为升级基线发布；后续优化在 `feature/1.1.1-jvm-optimization` 上维护。
2. GitHub Actions 建立 JDK 21/25 矩阵，运行 `mvn -B verify javadoc:javadoc --file pom.xml`。
3. 在两套 JDK 下记录 Framework 全模块构建结果；任何 JDK 25 特有失败先修复构建插件或测试。

完成标准：两套 CI 都可执行相同的验证命令，Java 21 仍是 `maven.compiler.release`。

### 阶段 2：依赖升级与 Starter 回归

1. 将 `spring.boot.version`、`spring-boot-maven-plugin.version` 统一升级到 4.1.0，保持
   Cloud 2025.1.2 和已发布的 Alibaba BOM；记录其尚未完成正式 4.1 认证的风险。
2. 上游发布后升级 Alibaba BOM 并记录版本、公告和验证结果；若发布前仍未发布，必须取得维护者的
   例外批准，且不得使用 SNAPSHOT。
3. 逐项执行 boot、webmvc、webflux、config、register、rpc、persistence、cache、eda、governance、
   security 的自动配置和测试；修复 Jakarta、Security、Servlet/WebFlux、Actuator 行为变化。
4. 更新兼容性矩阵和 `migration-1.1.md`，列出弃用项、破坏性变更及已知限制。

完成标准：JDK 21/25 的全模块 `verify` 与 Javadoc 均通过，且没有未解释的依赖覆盖。

### 阶段 3：Native 能力和 Runtime Hints（延期）

1. 复用 Spring Boot 4.1 的 Native Build Tools 路径，提供可选 `-Pnative`，不改变默认 JVM 构建。
2. 以已有 Native 原型的 base 应用为验证样本；将反射、代理、资源和序列化问题收敛为各 Starter
   的 Runtime Hints 与测试，而不是为每个应用复制配置。
3. 在 Linux amd64 构建 Native 可执行文件，验证启动、health、Nacos 配置/注册、数据库、缓存、RPC、
   鉴权和日志；arm64 独立列为后续 CI 目标。

完成标准：样本应用可由文档化命令构建并运行 Native，核心链路有自动化或可重复的集成验证。

### 阶段 4：JVM 25 OCI 镜像（1.1.1）

1. 在现有 Jib 所有权扩展、非 root `1001`、`${REGISTRY_URL}`、认证、版本/latest 标签和
   `/app/logs` 卷基础上扩展，不另建平行发布链路。
2. JVM profile 默认使用可配置的 JDK/JRE 25 基础镜像；Native profile 不在 1.1.1 范围内。
   运行时镜像，并显式保留 CA 证书、时区和必要字体。
3. 对两种镜像验证用户、入口命令、端口、health、配置和日志，并记录层复用率。

完成标准：两种镜像均以非 root 启动，现有 Registry 和日志约定不被破坏。

### 阶段 5：应用验证、基准与发布准备

1. 用候选 Framework 版本在本地升级至少一个受影响 base 应用；启动其配置中心与依赖服务，确认
   实际订阅预期 Nacos Data ID，再测试受保护接口等关键链路。
2. 记录 1.0.0 与 1.1 的镜像大小、压缩推送大小、启动时间和空载内存，注明环境和测量命令。
3. 完成升级指南、Jib 使用指南、版本矩阵、变更日志、回滚与降级清单。
4. 经维护者确认后推送候选分支；CI 成功且再次确认后才创建 Release、Maven Central 发布和分支合并。

完成标准：Issue #64 全部验收项有链接到测试、命令、基准和文档的证据；发布动作仍须单独审批。

## 当前风险登记

| 风险 | 处理 |
| --- | --- |
| Alibaba 尚无正式 Boot 4.1 BOM | 阶段 2 阻塞；追踪上游正式发布，不引入 SNAPSHOT |
| Native 对动态组件的闭包不足 | 以 Starter 级 Runtime Hints 和样本集成测试闭环 |
| JDK 25 仅在本地可用 | CI 矩阵作为合并门禁；本地另用 GraalVM 21 验证最低基线 |
| 镜像“瘦身”损坏运行环境 | 以 CA、时区、日志卷、非 root 和 health 的运行检查作为门禁 |
