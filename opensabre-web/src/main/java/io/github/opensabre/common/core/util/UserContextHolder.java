package io.github.opensabre.common.core.util;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

/**
 * 用户上下文
 */
public class UserContextHolder {
    /**
     * 用户名默认key
     */
    public final String KEY_USERNAME = "user_name";
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
     * 清空上下文
     */
    public void clear() {
        threadLocal.remove();
    }

}
