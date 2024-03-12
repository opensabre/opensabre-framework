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
    @Schema(title = "用户名", description = "Form提交时操作人的用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
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
