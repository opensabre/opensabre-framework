package io.github.opensabre.common.web.entity.convert;

/**
 * 对象转换
 * Form -> Po
 * QueryForm -> Param
 */
public interface EntityConverter {
    <F, P> P convert(F source, Class<P> entityClass);
}
