# Framework 1.1.4 升级说明

1.1.4 在 1.1.3 基础上新增两项框架能力：

- WebMVC/WebFlux Starter 统一提供 Springdoc 3 与 Knife4j 运行时；Gateway 根据当前 `lb://`
  路由动态生成服务文档清单，并在路由刷新后重建清单。
- 新增 `opensabre-starter-monitoring`，统一 Actuator 指标模型、内部 Token 采集、Prometheus
  只读查询和最小端点暴露默认值。

## 应用迁移

WebMVC 应用可以删除直接声明的 Springdoc/Knife4j 依赖，保留 `opensabre.rest.swagger.*`
元数据即可。Gateway 可以删除 `springdoc.swagger-ui.urls` 静态清单，通过
`opensabre.openapi.gateway.*` 配置开关、排除路由、文档路径和显示名。

控制面序列化的 Gateway Path 参数可使用 `patterns.N`；框架同时兼容 Gateway 简写生成的
`_genkey_N` 和单值 `pattern`。推荐在文档路由 metadata 中显式配置
`opensabre.openapi.enabled`、`opensabre.openapi.path` 和 `opensabre.openapi.name`。显式文档路由
优先于业务路由的路径推断；内部路由可设置 `opensabre.openapi.enabled=false` 排除。

普通应用引入 `opensabre-starter-monitoring` 后不需要启用采集器。控制面设置
`opensabre.monitoring.collector.enabled=true`，并将旧的
`opensabre.gateway-admin.prometheus.server-url` 改为
`opensabre.monitoring.prometheus.server-url`。Actuator metrics 不应匿名放行或配置独立固定密码；
采集端使用框架内部 Token，服务端统一校验 `ACTUATOR_METRICS_READ` 权限。

文档端点的匿名/鉴权策略仍由应用安全链决定；框架只提供能力，不自动扩大业务应用的匿名访问面。
