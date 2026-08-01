# 错误码目录

`opensabre-starter-governance` 会在应用完成启动后，异步上报基础错误码及应用声明的业务错误码。
0.7.1 起上报由[治理注册运行时](governance-registration.md)执行有限重试、指标和状态记录；
失败不影响应用可用性。

应用通过 `ErrorCatalogProvider` 声明业务枚举：

```java
@Bean
ErrorCatalogProvider orderErrorCatalogProvider() {
    return ErrorCatalogProvider.of("order", OrderErrorType.values());
}
```

所有参与上报的应用与 `base-sysadmin` 必须配置相同的 `ERROR_CATALOG_REGISTRATION_TOKEN`。目录端拒绝空凭据、无效凭据和被其他应用占用的错误码。

Framework 在发送快照前会补齐定义归属：业务 Provider 的错误码归当前应用所有，
范围为 `APPLICATION`；内置 `SystemErrorType` 归 `opensabre-framework` 所有，
范围为 `COMMON`。公共定义可由多个应用幂等上报，但内容必须完全一致。

管理端通过“系统管理 / 错误码目录”按错误码、文案、模块、应用与废弃状态查询；目录为声明快照，不会改变应用运行时的异常返回。
