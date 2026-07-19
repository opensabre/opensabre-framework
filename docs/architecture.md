# 架构与模块

框架采用 Maven 多模块结构，根 `pom.xml` 的 `<modules>` 是模块事实源。Starter 面向应用提供可组合的基础能力；`opensabre-base-dependencies` 管理依赖版本，`opensabre-test` 提供测试辅助，`opensabre-web` 提供通用 Web 能力。

模块自动配置、配置属性、条件装配和公共 API 以各模块 `src/main/java`、`src/main/resources/META-INF/` 及测试为准。文档不得把内部实现细节承诺为稳定公共 API，除非已在公共包和版本策略中明确。
