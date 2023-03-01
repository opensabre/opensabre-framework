package io.github.opensabre.common.test;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 测试工具类，辅助测试使用
 *
 * @author zhoutaoo
 */
public class PrivateHelper {

    private PrivateHelper() {
    }

    /**
     * 创建实例
     *
     * @return PrivateHelper实例
     */
    public static PrivateHelper getInstance() {
        return SingletPrivateHelper.sInstance;
    }

    /**
     * 静态内部类单例模式
     * 单例初使化
     */
    private static class SingletPrivateHelper {
        private static final PrivateHelper sInstance = new PrivateHelper();
    }

    /**
     * @param instance  实例对象
     * @param fieldName 成员变量名
     * @param value     值
     */
    public void setPrivateField(Object instance, String fieldName, Object value) {
        Field signingKeyField = ReflectionUtils.findField(instance.getClass(), fieldName);
        ReflectionUtils.makeAccessible(signingKeyField);
        ReflectionUtils.setField(signingKeyField, instance, value);

    }

    /**
     * 寻找对象有参方法
     *
     * @param instance       实例对象
     * @param methodName     方法名
     * @param parameterTypes 方法参数类型
     * @return 对象方法
     */
    public Method findMethod(Object instance, String methodName, Class<?>... parameterTypes) {
        return ReflectionUtils.findMethod(instance.getClass(), methodName, parameterTypes);
    }

    /**
     * 寻找对象无参方法
     *
     * @param instance   实例对象
     * @param methodName 方法名
     * @return 对象方法
     */
    public Method findMethod(Object instance, String methodName) {
        return ReflectionUtils.findMethod(instance.getClass(), methodName);
    }

    /**
     * 将么有方法设置为可访问，并调用该方法
     *
     * @param instance 实例对象
     * @param method   方法对象
     * @param args     实例的参数
     * @return 对象
     */
    public Object invokePrivateMethod(Object instance, Method method, Object... args) {
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, instance, args);
    }
}
