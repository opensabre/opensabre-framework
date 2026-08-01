# 治理注册运行时

Framework 0.7.1 统一调度错误码目录和字典快照注册。注册仍在应用就绪后异步执行；
Sysadmin 暂时不可用不会阻塞应用启动，任务会按有限次数指数退避重试。

```yaml
opensabre:
  governance:
    registration:
      max-attempts: 4
      initial-backoff: 1s
      max-backoff: 30s
      jitter: 0.2
      pool-size: 2
      thread-name-prefix: governance-registration-
      wait-for-tasks-to-complete-on-shutdown: true
      await-termination: 35s
```

同一类型任务不会并发执行。应用关闭时由 Spring 管理注册调度器，并在
`await-termination` 窗口内等待已接收和已安排重试的任务；超过窗口后关闭仍会继续，
不会无限阻塞停机。每次尝试产生以下 Micrometer 指标：

- `opensabre.governance.registration.attempts`，标签为 `task` 和 `result`
- `opensabre.governance.registration.duration`，标签为 `task`

Actuator 端点 `opensabreGovernanceRegistration` 提供最近一次开始、完成、成功、失败、
下一次重试时间及累计次数。端点默认仍受 Spring Boot Actuator 暴露与安全配置控制：

```http
GET  /actuator/opensabreGovernanceRegistration
POST /actuator/opensabreGovernanceRegistration?task=error-catalog
POST /actuator/opensabreGovernanceRegistration?task=dictionary
```

写操作用于运维手动刷新；任务已运行或等待重试时不会重复提交。
