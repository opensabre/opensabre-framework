# Starter 模块地图

| 模块 | 介绍与使用要点 | 规划重点 |
| --- | --- | --- |
| `opensabre-base-dependencies` | 统一依赖版本；应用通过依赖管理引入。 | 维护 Spring Cloud 兼容矩阵。 |
| `opensabre-starter-boot` | 应用启动基础能力。 | 固化基础配置与审计接入说明。 |
| `opensabre-starter-cache` | 缓存相关自动配置。 | 补齐失效、序列化和测试规范。 |
| `opensabre-starter-config` | 配置能力。 | 明确配置优先级与敏感信息策略。 |
| `opensabre-starter-register` | 服务注册/发现集成。 | 固化环境接入与故障排查。 |
| `opensabre-starter-rpc` | RPC 集成。 | 明确契约、超时和兼容性策略。 |
| `opensabre-starter-persistence` | 持久化能力。 | 沉淀事务、迁移与审计约定。 |
| `opensabre-starter-eda` | 事件驱动架构能力；见模块内 README。 | 固化事件契约、幂等与可观测性。 |
| `opensabre-starter-governance` | 治理与审计相关能力。 | 补齐限流、审计、容错集成说明。 |
| `opensabre-starter-webmvc` | Servlet Web 应用能力；见模块内 README。 | 明确 MVC 扩展点与异常契约。 |
| `opensabre-starter-webflux` | 响应式 Web 能力；见模块内 README。 | 明确响应式边界与阻塞调用限制。 |
| `opensabre-test` | 测试支持。 | 扩展集成测试样板。 |
| `opensabre-web` | 通用 Web 能力；见模块内 README。 | 稳定响应、异常、校验等公共契约。 |

每个 starter 后续应补充独立文件，结构统一为：介绍、依赖与配置、使用示例、生命周期/流程、兼容性、规划。
# 模块文档

- [治理计次与限次接入](governance-usage.md)
- [错误码目录](error-catalog.md)
