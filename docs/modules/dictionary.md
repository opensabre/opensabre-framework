# 字典治理组件

`opensabre-starter-governance` 0.7.0 提供应用侧字典声明、启动注册、预热和本地缓存读取能力。

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
