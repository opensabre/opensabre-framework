# 字典治理组件

`opensabre-starter-governance` 0.7.6 提供应用侧字典声明、启动注册、预热和本地缓存读取能力。

## 配置

```yaml
opensabre:
  governance:
    dictionary:
      enabled: true
      registration-enabled: true
      registration-token: ${DICTIONARY_REGISTRATION_TOKEN:${GOVERNANCE_REGISTRATION_TOKEN:${ERROR_CATALOG_REGISTRATION_TOKEN:}}}
      preload-codes: [order_status]
```

`registration-enabled` 默认开启；没有声明 `DictionaryProvider` 的应用不会发起注册请求。
字典与错误码上报默认共享 `GOVERNANCE_REGISTRATION_TOKEN`，也可通过
`DICTIONARY_REGISTRATION_TOKEN` 单独覆盖。`ERROR_CATALOG_REGISTRATION_TOKEN` 仅作为旧部署兼容回退。
注册任务使用[治理注册运行时](governance-registration.md)执行有限重试并暴露运行状态。

## 声明与读取

### 标准枚举自动注册

推荐使用 `DictionaryEnum` + `@OpenSabreDictionary` 声明固定枚举字典：

```java
@OpenSabreDictionary(code = "gender", name = "用户性别")
public enum Gender implements DictionaryEnum {
    FEMALE("F", "女"),
    MALE("M", "男");

    private final String value;
    private final String label;

    Gender(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override public String value() { return value; }
    @Override public String label() { return label; }
}
```

治理 Starter 默认扫描 Spring Boot 应用的自动配置包。也可以显式指定：

```yaml
opensabre:
  governance:
    dictionary:
      scan-packages:
        - com.example.user.enums
```

只有同时满足以下条件的类型才会自动上报：

- 是 `enum`；
- 实现 `DictionaryEnum`；
- 标注 `@OpenSabreDictionary`。

枚举声明顺序默认作为 `sort`，实现 `DictionaryEnum.sort()` 可覆盖顺序，`tagType()` 可设置标签类型。
已有的 `DictionaryProvider` 仍然兼容，适用于动态字典、非枚举字典或需要自定义转换的场景。

```java
@Bean
DictionaryProvider orderDictionaryProvider() {
    return DictionaryProvider.of(DictionaryDefinition.of(
            "order_status", "订单状态", OrderStatus.values(),
            OrderStatus::getCode, OrderStatus::getLabel));
}
```

```java
List<DictionaryItem> items = dictionaryService.items("order_status");
String label = dictionaryService.labelOf("order_status", value).orElse("未知状态");
boolean valid = dictionaryService.contains("order_status", value);
dictionaryService.refresh("order_status");
```

从 0.7.6 开始，接口 DTO 可以直接使用字典约束校验启用项：

```java
public record CreateUserRequest(
        @NotBlank
        @DictionaryValue(value = "gender", message = "性别必须是有效字典项")
        String gender) {
}
```

Controller 继续使用标准 Jakarta Validation：

```java
@PostMapping
public Result<Boolean> create(@Valid @RequestBody CreateUserRequest request) {
    // ...
}
```

`@DictionaryValue` 复用 `DictionaryService.contains`，因此只接受启用项并自动使用本地字典缓存。
空值不由该注解处理，应按字段要求组合 `@NotNull`、`@NotBlank` 等标准约束。Sysadmin 不可用时
保留 `DictionaryUnavailableException`，不能把基础设施故障伪装成普通参数非法。默认 WebMVC
全局异常处理区分两类结果：值不属于启用项时返回 `ARGUMENT_NOT_VALID`；字典加载异常被校验引擎
包装后返回 HTTP 503 和 `SYSTEM_BUSY`。Controller 参数与方法级校验产生的
`HandlerMethodValidationException`、`ConstraintViolationException` 也统一映射为参数校验错误。

- `items` 只返回启用项。
- `labelOf` 包含停用项，用于历史值回显。
- `contains` 只校验启用项。
- `refresh` 清理本地缓存，下次读取重新加载。

默认实现使用 JetCache `shortTime` 本地缓存，缓存未命中时调用 Sysadmin。加载失败抛出 `DictionaryUnavailableException`，不能当作“字典值非法”处理。

## 后端协议

Framework 客户端约定：

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| `POST` | `/dicts/snapshots` | 携带 `X-Opensabre-Dictionary-Token` 注册应用字典快照 |
| `GET` | `/dicts/{dictCode}/items/all` | 返回包含停用项的完整字典项列表 |

`base-sysadmin` 已实现以上治理协议。应用启动注册采用尽力而为语义：注册失败会记录日志，
但不会阻止应用启动；读取失败仍抛出 `DictionaryUnavailableException`，由业务决定降级策略。

## 后端工作流程

### 1. 应用声明

业务应用通过一个或多个 `DictionaryProvider` Bean 声明自己拥有的字典。Framework 会收集所有
Provider，以 `dictCode` 合并定义；同一应用内出现内容不同的重复编码时，注册任务失败并记录冲突，
不会向 Sysadmin 发送不确定的定义。

应用只声明稳定的业务值、展示标签、排序和标签样式，不在本地建立平行的字典持久化模型。需要由
运营人员维护、且不属于某个应用的字典，继续由 Sysadmin 字典管理功能维护。

### 2. 启动注册

应用触发 `ApplicationReadyEvent` 后，`DictionaryRegistrationListener` 执行以下步骤：

1. 汇总所有 `DictionaryProvider`；没有 Provider 时不发起请求。
2. 从 `spring.application.name` 取得应用名并生成完整字典快照。
3. 通过 `SysadminGovernanceClient` 调用 `POST /dicts/snapshots`。
4. 在 `X-Opensabre-Dictionary-Token` 请求头携带注册凭据。
5. 交由通用治理注册运行时执行有限次数、指数退避且带抖动的重试。

注册采用 fail-open 语义：Sysadmin 暂时不可用不会阻止业务应用启动。注册结果、尝试次数和最近错误
由治理注册运行时记录，日志中的 `Governance registration task succeeded: task=dictionary` 表示本轮完成。

```text
DictionaryProvider
        │
        ▼
ApplicationReadyEvent
        │
        ▼
合并应用完整快照 ──注册 Token──▶ Sysadmin /dicts/snapshots
```

### 3. 运行时读取

`DictionaryService` 默认由 `JetCacheDictionaryService` 实现。每个应用使用
`governance:dictionary:{dictCode}` 本地缓存，缓存未命中时读取
`GET /dicts/{dictCode}/items/all`，将包含停用项的完整结果缓存后再按调用语义过滤：

- `items`：只返回启用项，适合选项列表。
- `contains`：只认可启用项，适合新数据校验。
- `labelOf`：包含停用项，保证历史业务值仍可回显。
- `refresh`：删除指定字典的本地缓存，下次访问重新加载。

本地缓存开启缓存击穿保护。远程加载失败会抛出 `DictionaryUnavailableException`；调用方必须把
“字典服务不可用”和“业务值不合法”区分处理。

### 4. 预热与刷新

`preload-codes` 中的字典在应用就绪后预热。单个字典预热失败只记录警告，不影响其他字典和应用启动。
当前版本没有完成 Sysadmin 变更向所有后端应用实时广播的闭环；变更通过短时缓存自然过期或显式调用
`refresh(dictCode)` 生效。后续实时同步应发布包含 `dictCode` 的变更事件，由消费者精准清除本地缓存。
