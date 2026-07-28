package io.github.opensabre.common.core.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户上下文
 */
public class UserContextHolder {
    /**
     * 用户名默认key
     */
    public final String KEY_USERNAME = "user_name";
    /**
     * 用户唯一标识默认key
     */
    public final String KEY_USER_ID = "user_id";
    /**
     * 角色快照默认key
     */
    public final String KEY_ROLES = "roles";
    /**
     * Scope快照默认key
     */
    public final String KEY_SCOPES = "scopes";
    /**
     * 用于存储线程相关变量
     */
    private final ThreadLocal<Map<String, String>> threadLocal;

    /**
     * 默认构造方法
     */
    private UserContextHolder() {
        this.threadLocal = new ThreadLocal<>();
    }

    /**
     * 创建实例
     *
     * @return UserContextHolder单例
     */
    public static UserContextHolder getInstance() {
        return SingletonHolder.sInstance;
    }

    /**
     * 静态内部类单例模式
     * 单例初使化
     */
    private static class SingletonHolder {
        /**
         * 使用静态变量返回单例
         */
        private static final UserContextHolder sInstance = new UserContextHolder();
    }

    /**
     * 用户上下文中放入信息
     *
     * @param map 上下文context参数
     */
    public void setContext(Map<String, String> map) {
        threadLocal.set(map);
    }

    /**
     * 获取上下文中的信息
     *
     * @return 返回上下文map
     */
    public Map<String, String> getContext() {
        return Optional.ofNullable(threadLocal.get()).orElse(Maps.newHashMap());
    }

    /**
     * 获取上下文中的用户名
     *
     * @return 操作用户的用户名
     */
    public String getUsername() {
        return Optional.ofNullable(threadLocal.get()).orElse(Maps.newHashMap()).get(KEY_USERNAME);
    }

    /**
     * 获取当前用户唯一标识。
     *
     * @return 用户唯一标识
     */
    public String getUserId() {
        return getValue(KEY_USER_ID);
    }

    /**
     * 获取当前角色快照。
     *
     * @return 不可变角色集合
     */
    public Set<String> getRoles() {
        return splitValues(getValue(KEY_ROLES));
    }

    /**
     * 获取当前Scope快照。
     *
     * @return 不可变Scope集合
     */
    public Set<String> getScopes() {
        return splitValues(getValue(KEY_SCOPES));
    }

    /**
     * 获取指定用户上下文字段。
     *
     * @param key 上下文键
     * @return 字段值
     */
    public String getValue(String key) {
        return Optional.ofNullable(threadLocal.get()).orElse(Maps.newHashMap()).get(key);
    }

    /**
     * 清空上下文
     */
    public void clear() {
        threadLocal.remove();
    }

    private Set<String> splitValues(String values) {
        if (values == null || values.isBlank()) {
            return Set.of();
        }
        return Stream.of(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
