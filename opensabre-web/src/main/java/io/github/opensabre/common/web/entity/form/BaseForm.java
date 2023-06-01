package io.github.opensabre.common.web.entity.form;

import io.github.opensabre.common.web.entity.convert.EntityModelConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema
public class BaseForm<P> implements Serializable {
    /**
     * 用户名
     */
    private String username;

    /**
     * Form转化为Po
     *
     * @param clazz Po类
     * @return 返回Po
     */
    public P toPo(Class<P> clazz) {
        return EntityModelConverter.getInstance().convert(this, clazz);
    }
}
