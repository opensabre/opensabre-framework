# Governance 使用量接入

`opensabre-starter-governance` 为验证码、限次与通知提供统一的使用量统计入口。

## 选择方式

- 默认 `EDA`：适用于验证码、通知和其他不应影响主业务的异步观测。应用自行引入并实现 EDA 的 `EventTransport`，可替换 Spring Cloud Stream、RabbitMQ 或 Kafka。
- `HTTP`：设置 `opensabre.governance.usage.transport=HTTP`，starter 通过 Sysadmin 的 HTTP 接口受理记录。
- 限次判定：注入 `GovernanceRateLimiter`，它是同步调用，EDA 只能用于限次观测，不能用于放行判定。

## 类型化 API

```java
captchaUsageRecorder.generateSuccess("login");
rateLimitUsageRecorder.allowed("api-login");
notificationUsageRecorder.templateSendFailure("password-reset");
```

也可注入 `UsageCounterRecorder` 记录自定义对象：

```java
usageCounterRecorder.success("REPORT", reportId, "EXPORT");
```

每条跨服务记录应携带稳定的 `recordId` 并在重试时复用；不得写入验证码、通知正文、手机号等敏感数据。

## 限次同步判定

```java
RateLimitDecision decision = governanceRateLimiter.check(request);
if (!decision.allowed()) {
    // 拒绝请求
}
```

限次服务不可用时是否放行由 `opensabre.governance.ratelimit.fail-open` 控制。
