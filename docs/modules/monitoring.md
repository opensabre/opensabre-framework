# 应用监控组件

`opensabre-starter-monitoring` 统一 OpenSabre 应用的 Actuator 指标面、控制面采集和 Prometheus
查询边界。普通应用引入 starter 后获得安全的端点暴露默认值；只有控制面需要开启采集器。

## 指标访问与内部 Token

监控不使用独立用户名密码，也不匿名放行 `/actuator/metrics/**`。`base-gateway-admin` 使用
`ActuatorMonitoringTokenIssuer` 为目标服务签发短期内部 Token，被监控应用通过
`ActuatorMonitoringAccess` 统一限定路径和 `ACTUATOR_METRICS_READ` Authority。

指标契约包括健康状态、进程 CPU、堆内存已用/上限、活动线程和进程运行时长。单个实例不可用时，
`ApplicationMonitoringService` 返回该实例的错误信息，不影响同一服务的其他实例。

## 控制面配置

```yaml
opensabre:
  monitoring:
    collector:
      enabled: true
      connect-timeout: 2s
      read-timeout: 3s
    prometheus:
      server-url: http://prometheus:9090
```

`collector.enabled` 缺省为 `false`，普通应用不会发起采集。`PrometheusReadClient` 仅执行 HTTP
GET 即时查询；框架提供固定查询模板，不创建监控数据库或持久化查询结果。

## 从应用侧临时实现迁移

升级到 1.1.4 后，业务应用只需引入 `opensabre-starter-monitoring`，删除自行声明的
`micrometer-registry-prometheus` 以及重复的 Actuator/Prometheus HTTP 客户端。控制面原配置
`opensabre.gateway-admin.prometheus.server-url` 迁移为
`opensabre.monitoring.prometheus.server-url`，并显式设置
`opensabre.monitoring.collector.enabled=true`。服务发现仍由控制面适配 Nacos 实例，框架监控模型
只依赖标准的 host、port、health 和 management 元数据，因此不会把注册中心实现写死在 starter 中。

## Actuator 暴露面

starter 默认仅暴露 `health,info,metrics,prometheus,internalTokenKeyStatus`，并关闭
`env`、`configprops`、`beans`、`heapdump`、`threaddump` 和 `shutdown`。应用安全链仍必须对指标路径
要求 `ActuatorMonitoringAccess.AUTHORITY`；健康检查是否匿名开放由应用自行决定。
