# OpenSabre框架审计日志使用指南

## 概述

OpenSabre框架提供了内置的审计日志功能，能够自动记录用户的关键操作行为，包括操作人、操作时间、操作内容、操作结果等信息，满足安全审计和合规性要求。

## 核心功能

### 1. 操作记录
- 记录用户的关键操作（增、删、改）
- 记录操作前后的数据变化
- 记录操作执行时间
- 记录操作结果（成功/失败）

### 2. 安全审计
- 记录操作人身份信息
- 记录客户端IP地址
- 记录用户代理信息
- 记录请求参数

### 3. 自动集成
- 与Spring Security自动集成
- 自动获取操作人信息
- 自动记录请求上下文

## 启用审计日志

### 1. 启用全局审计日志

在应用启动类上添加`@EnabledAudit`注解：

```java
@EnabledAudit
@SpringBootApplication
public class OrganizationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrganizationApplication.class, args);
    }
}
```

### 2. 使用方法级审计日志

在需要记录审计日志的Controller方法上添加`@Audit`注解：

```java
@Audit(
    operationType = OperationType.CREATE,
    description = "新增用户",
    module = "USER",
    response = true, 
    key = "#userForm.username"
)
@PostMapping
public boolean add(@RequestBody UserForm userForm) {
    return userService.add(user);
}
```

### 3. 注解参数说明

| 参数名           | 类型 | 必填 | 说明 |
|---------------|------|------|------|
| operationType | OperationType | 是 | 操作类型枚举（CREATE/UPDATE/DELETE/QUERY等） |
| description   | String | 是 | 操作描述 |
| module        | String | 否 | 操作模块 |
| response      | boolean | 否 | 是否记录响应结果，默认false |
| key           | String | 否 | 操作目标键，支持SpEL表达式 |

## 集成示例

### 用户管理模块

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @Audit(
        operationType = OperationType.CREATE,
        description = "新增用户",
        module = "USER",
        response = true,
        key = "#userForm.username"
    )
    @PostMapping
    public boolean add(@RequestBody UserForm userForm) {
        return userService.add(userForm);
    }

    @Audit(
        operationType = OperationType.UPDATE,
        description = "修改用户信息",
        module = "USER",
        response = true,
        key = "#userForm.username"
    )
    @PutMapping("/{id}")
    public boolean update(@PathVariable String id, @RequestBody UserForm userForm) {
        return userService.update(id, userForm);
    }

    @Audit(
        operationType = OperationType.DELETE,
        description = "删除用户",
        module = "USER",
        response = true,
        key = "#id"
    )
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String id) {
        return userService.delete(id);
    }
}
```

## 操作类型枚举

OpenSabre框架提供了标准的操作类型枚举：

```java
public enum OperationType {
    CREATE,     // 创建
    UPDATE,     // 更新
    DELETE,     // 删除
    QUERY,      // 查询
    LOGIN,      // 登录
    LOGOUT,     // 登出
    EXPORT,     // 导出
    IMPORT,     // 导入
    SCAN,       // 扫描
    DOWNLOAD,   // 下载
    UPLOAD      // 上传
}
```

## 最佳实践

### 1. 记录范围
- 只记录关键业务操作
- 避免记录查询操作（除非特别重要）
- 敏感操作必须记录

### 2. SpEL表达式使用
- 使用`#参数名`引用方法参数
- 支持对象属性访问，如`#userForm.username`
- 支持方法调用，如`#userForm.getId()`

### 3. 模块命名规范
- 使用大写字母
- 保持简洁明了
- 建议使用实体名称

## 注意事项

1. **依赖要求**：确保项目中包含OpenSabre框架的审计日志模块
2. **事件处理**：默认处理器会记录 `AuditEvent` 日志；如需入库，需要在业务侧自定义 `ApplicationListener<AuditEvent>`
3. **性能**：审计事件在操作完成后发布，持久化等耗时处理建议放到自定义监听器中异步执行
4. **安全性**：审计日志记录操作人信息需要Spring Security支持

## 故障排除

### 常见问题

1. **审计日志未记录**
   - 检查`@EnabledAudit`注解是否启用
   - 检查`@Audit`注解配置是否正确
   - 验证Spring Security配置

2. **操作人信息为空**
   - 检查用户认证是否成功
   - 验证Spring Security上下文

3. **SpEL表达式错误**
   - 检查表达式语法
   - 验证参数名称是否正确
