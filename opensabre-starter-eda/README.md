# OpenSabre EDA Starter

`opensabre-starter-eda` 提供进程内异步事件分发；不依赖 RabbitMQ。业务代码通过 `EdaEventPublisher` 发布事件，并实现 `EdaEventHandler<T>` 订阅稳定的 `eventType`。

```java
publisher.publishLocal(EdaEvent.of("order.created", "order-service", new OrderCreated(...)));
```

```java
@Component
class OrderCreatedHandler implements EdaEventHandler<OrderCreated> {
    public String eventType() {
        return "order.created";
    }

    public void handle(EdaEvent<OrderCreated> event) {
        // 对 eventId 的副作用处理应保持幂等
    }
}
```

在事务方法中使用 `publishAfterCommit`，使事件只在事务成功提交后发布。它不是事务 Outbox：进程在提交后、发送前崩溃时仍可能丢失事件。

```yaml
opensabre:
  eda:
    local:
      core-pool-size: 2
      max-pool-size: 8
      queue-capacity: 1000
      thread-name-prefix: eda-event-
    publisher:
      remote-required: false
```

本地处理器的异常和队列拒绝只记录日志与指标，不改变发布线程的默认业务结果。指标包括 `opensabre.eda.events.*` 和 `opensabre.eda.executor.*`；禁止将业务对象 ID、eventId 或完整 payload 用作指标标签。

## 跨服务传输

EDA 核心不引入 RabbitMQ、Kafka、RocketMQ 或任何 MQ 客户端。应用按需引入客户端，并声明一个或多个 `EventTransport` Bean；调用 `publisher.publishRemote(event)` 时，EDA 会调用这些 Bean。

```java
@Component
class ApplicationMqTransport implements EventTransport {
    @Override
    public void publish(EdaEvent<?> event) {
        // 使用应用自行引入的 MQ 客户端序列化并发送 event
    }
}
```

应用的 MQ 消费端收到消息后，将反序列化得到的事件调用 `publisher.publishLocal(event)`，即可复用相同的本地处理器。生产端/消费端的确认、重试、死信、消息持久化和 JSON/Avro/Protobuf 协议都由应用的 transport 实现决定。

使用 `publisher.publishRemote(event)` 或 `publisher.publish(event, EventTarget.LOCAL, EventTarget.REMOTE)` 发布。消费者应根据 `eventId` 保证副作用幂等。事务 Outbox、全局去重或端到端精确一次不属于 EDA core；支付、计费、配额扣减等关键流程应由应用的 transport 或 Outbox 实现保证可靠性。
