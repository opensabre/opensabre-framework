# 字典治理组件

`opensabre-starter-governance` 0.7.0 提供应用侧字典声明、启动注册、预热和本地缓存读取能力。

## 配置

```yaml
opensabre:
  governance:
    dictionary:
      enabled: true
      registration-enabled: false
      registration-token: ${DICTIONARY_REGISTRATION_TOKEN:${ERROR_CATALOG_REGISTRATION_TOKEN:}}
      preload-codes: [order_status]
```

`registration-enabled` 默认关闭。只有后端实现字典快照注册协议后才可开启。

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

截至 0.7.0 发布时，`base-sysadmin` 的当前发布分支仅提供字典 CRUD 与 options API，尚未实现以上两个治理协议端点。因此读取/注册链路在接入前必须先确认后端兼容版本；未补齐时保持 `registration-enabled=false`。
跟踪 Issue：[https://github.com/opensabre/base-sysadmin/issues/10](https://github.com/opensabre/base-sysadmin/issues/10)
