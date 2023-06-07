package io.github.opensabre.common.web.entity.convert;

/**
 * 对象转换
 * Form -》 Po
 * QueryForm -》 Param
 */
public interface EntityConverter {
    /**
     * 对象转换器接口
     *
     * @param source      源对象
     * @param entityClass 目标对象类
     * @param <F>         源对象类型
     * @param <P>         目标对象类型
     * @return P对象
     */
    <F, P> P convert(F source, Class<P> entityClass);
}
