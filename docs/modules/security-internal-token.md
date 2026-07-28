# 内部 Token 安全组件

`opensabre-starter-security` 从 0.7.0 开始提供 OpenSabre 服务间内部 Token 的协议和核心实现。

## 边界

- 浏览器继续使用 Session。
- 网关继续校验并透传外部 OAuth JWT，不换发内部 Token。
- 首应用校验外部 JWT、完成接口权限判断后，签发第一个内部 Token。
- 每次服务调用都面向目标服务重新签发 Token，禁止原样转发上一跳 Token。
- 内部 Token 固定使用 `x-client-token` Header。

当前实现提供 HS256 签发、active/previous 双密钥验证、claims 校验、
`UserContextHolder` 绑定、MVC 入站校验、Feign/RestClient 逐跳重签，
以及密钥状态和轮换的框架 SPI。管理端已接入共享配置后端、管理 API 和页面，
密钥操作复用 OpenSabre Governance 通用审计能力。
WebFlux/WebClient 暂不实现。

## 共享配置

所有应用从统一配置中心读取同一份密钥配置。密钥必须使用 Base64 编码，解码后不得少于 32 字节。

```yaml
opensabre:
  security:
    internal-token:
      enabled: true
      # RestClient 全局拦截默认关闭；开启后仍只处理白名单中的内部目标
      rest-client-enabled: true
      rest-client-allowed-targets:
        - base-order
        - base-sysadmin
      # 纯内部应用可设为 true；同时承接网关 JWT 的应用保持 false
      required: false
      key-config-version: 1
      active-key-id: key-202607
      active-key: ${OPENSABRE_INTERNAL_TOKEN_ACTIVE_KEY}
      active-key-activated-at: 2026-07-25T00:00:00Z
      previous-key-id: key-202606
      previous-key: ${OPENSABRE_INTERNAL_TOKEN_PREVIOUS_KEY}
      previous-key-retire-after: 2026-07-25T00:05:00Z
      ttl: 60s
      max-ttl: 120s
      clock-skew: 5s
      max-hop: 8
      max-token-bytes: 8192
      max-extension-bytes: 2048
      excluded-paths:
        - /actuator/**
        - /v3/**
      allowed-issuers:
        - base-organization
        - base-sysadmin
      allowed-extension-keys:
        - tenant
        - locale
```

配置中心轮换时，新密钥成为 active，旧 active 成为 previous。所有应用确认加载新
`key-config-version` 后，previous 仍须至少保留最大 Token 生命周期与时钟偏差之和。
安全 Starter 默认通过配置中心共享配置 `opensabre-security.yml` 加载这份配置，
Data ID 和 Group 可分别用 `OPENSABRE_SECURITY_CONFIG_DATA_ID`、
`OPENSABRE_SECURITY_CONFIG_GROUP` 覆盖。

## Claims

Token 是 `typ=OS-INTERNAL`、`alg=HS256` 的 compact JWS。核心字段包括：

- `iss`、`src`：真实签发服务。
- `aud`、`dst`：目标服务。
- `sub`、`username`：当前操作用户。
- `jti`、`parent_jti`：本跳和上一跳 Token 标识。
- `iat`、`nbf`、`exp`：有效期，硬上限 120 秒。
- `scope`、`roles`：授权快照。
- `hop`：调用跳数。
- `trace_id`：链路标识。
- `key_config_version`：共享密钥配置版本。
- `ext`：白名单限制的扩展对象。

接收方必须验证签名、`kid`、issuer、audience、source、destination、时间和 hop。
`ext` 不能替代核心安全字段。

## 核心使用

```java
InternalTokenRequest request = new InternalTokenRequest(
        "base-organization",
        "user-id",
        "username",
        "base-order",
        List.of("order:read"),
        List.of("admin"),
        1,
        previousTokenId,
        traceId,
        Map.of("tenant", "tenant-a"));

String token = internalTokenService.issue(request);
InternalTokenClaims claims = internalTokenService.verify(token, "base-order");
internalTokenUserContext.bind(claims);
```

验证成功后可通过 `UserContextHolder` 获取可信上下文：

```java
String userId = UserContextHolder.getInstance().getUserId();
String username = UserContextHolder.getInstance().getUsername();
Set<String> roles = UserContextHolder.getInstance().getRoles();
Set<String> scopes = UserContextHolder.getInstance().getScopes();
String tenant = UserContextHolder.getInstance().getValue("ext.tenant");
```

请求结束时必须调用 `InternalTokenUserContext.clear()`，避免线程复用造成上下文泄漏。

## 首应用可信身份

首应用签发第一跳内部 Token 时，默认只从已认证的 Spring Security
`Authentication` 读取 subject、username、直接 role authority、`ROLE_` 角色和
`SCOPE_` 权限。
不再使用直接解码但未校验签名的外部 JWT 内容生成内部 Token。

如果应用不使用 Spring Security，必须提供自己的 `InternalTokenPrincipalProvider` Bean，
并保证它只返回已经完成认证和接口权限校验的身份。没有可信 Principal 时，框架签发
`sub=service:<spring.application.name>` 的服务身份 Token，不会把普通
`UserContextHolder` 字段提升为可信用户身份。

## MVC 入站校验

Servlet 应用引入安全 Starter 后会自动注册内部 Token 拦截器：

- `enabled=true` 且请求带 `x-client-token`：严格验证，成功后建立可信用户上下文。
- `enabled=true` 且请求不带内部 Token：默认继续由外部 JWT/接口权限链处理。
- `required=true`：缺失内部 Token 直接返回 401，仅适用于纯内部应用。
- `enabled=false`：全局跳过内部 Token 校验，但不会信任旧的 `x-client-token-user`。

0.7.0 不再读取未签名的 `x-client-token-user`，也不会从 `x-client-token`
直接解码用户信息。

### Spring Security 接入

启用 Spring Security 的 Servlet 应用还需要把 Starter 提供的过滤器加入自身安全链，
使内部调用在 URL/方法权限判断前成为已认证请求：

```java
@Bean
SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        InternalTokenAuthenticationFilter internalTokenFilter) throws Exception {
    http.addFilterBefore(
            internalTokenFilter,
            BearerTokenAuthenticationFilter.class);
    return http.build();
}
```

过滤器把内部 Token 的 `roles` 原样映射为 Authority，把 `scopes` 映射为
`SCOPE_<scope>`，并同步绑定 `UserContextHolder`。外部 Bearer JWT 和内部 Token
不能同时出现在同一请求中：网关到首应用只携带外部 JWT，后续服务调用只携带逐跳
重签的 `x-client-token`。

Starter 会关闭该过滤器的独立 Servlet 自动注册，避免它运行在 Spring Security
上下文生命周期之外；没有 `SecurityFilterChain` 的应用仍由 MVC 拦截器完成验证。

## Feign 逐跳重签

`opensabre-starter-rpc` 引入安全 Starter。每次 Feign 调用都会：

1. 删除请求中原有的 `Authorization`、`x-client-token` 和 `x-client-token-user`。
2. 使用 `FeignClient` 的目标服务名作为 `aud`/`dst`。
3. 以当前应用名作为 `iss`/`src`。
4. 生成新的 `jti`，将上一跳 `jti` 写入 `parent_jti`，并增加 `hop`。
5. 将新 Token 写入 `x-client-token`。

`spring.application.name` 和 Feign 目标服务名缺失时拒绝签发，避免生成无目标 Token。

## RestClient 逐跳重签

内部 Token 与 RestClient 集成均显式开启，并且目标服务位于
`rest-client-allowed-targets` 白名单时，Servlet 应用通过 Spring Boot 注入的
`RestClient.Builder` 创建客户端会执行逐跳重签：

1. 删除原有的 `Authorization`、`x-client-token` 和 `x-client-token-user`。
2. 默认以请求 URI 的 host 作为目标服务名。
3. 基于可信用户上下文生成面向目标服务的新内部 Token。

```java
@Bean
RestClient orderRestClient(RestClient.Builder builder) {
    return builder.baseUrl("http://base-order").build();
}
```

默认情况下 RestClient 集成关闭，不会删除外部 `Authorization`，也不会向第三方服务
发送内部 Token。直接调用 `RestClient.builder()` 不会经过 Spring Boot 的自定义器。存在服务别名或特殊
寻址规则时，可提供自己的 `InternalTokenTargetResolver` Bean，将请求 URI 映射为真实
的 `aud`/`dst` 服务名。

本迭代不注册 WebClient filter，也不提供 WebFlux 入站校验。

## 密钥状态与轮换 SPI

框架只负责定义安全边界，共享配置中心的写入适配器由管理应用实现：

- `InternalTokenKeyStatusProvider`：读取当前应用可见的配置版本和 active/previous key id，
  只暴露“是否已配置”，不暴露密钥。
- `InternalTokenKeyRotationHandler`：生成至少 256 bit 随机密钥，并以乐观锁更新共享配置；
  轮换接口不接收也不返回密钥内容。

密钥管理接口使用 Governance Starter 已有的 `@Audit` 记录操作人、原因、时间、
目标 key id、请求来源、结果和失败信息，统一写入 `base_sys_audit_log`。审计请求和
响应只包含安全元数据，严禁记录密钥或完整 Token。

## 安全约束

- 不在代码库、日志、审计记录或管理页面返回共享密钥和完整 Token。
- `enabled` 默认关闭；应用完成共享密钥配置和调用链验证后再显式开启。
- 共享 HMAC 意味着任一持有密钥的应用理论上具有全局签发能力，需要配合网络隔离、
  最小配置读取权限、短 TTL、快速轮换和完整审计。
