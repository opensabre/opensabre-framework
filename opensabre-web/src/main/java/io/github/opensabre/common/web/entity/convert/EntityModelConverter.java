package io.github.opensabre.common.web.entity.convert;

import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;

public class EntityModelConverter implements EntityConverter {
    private EntityModelConverter() {
    }

    /**
     * 返回实例
     *
     * @return 单例
     */
    public static EntityModelConverter getInstance() {
        return SingletonHolder.sInstance;
    }

    /**
     * 静态内部类单例模式
     * 单例初使化
     */
    private static class SingletonHolder {
        private static final EntityModelConverter sInstance = new EntityModelConverter();
    }

    /**
     * 将源对象转换为 目标对象
     *
     * @param source      源对象
     * @param targetClass 目标对象类型
     * @return 目标对象
     */
    @SneakyThrows
    public <F, P> P convert(F source, Class<P> targetClass) {
        P target = targetClass.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
