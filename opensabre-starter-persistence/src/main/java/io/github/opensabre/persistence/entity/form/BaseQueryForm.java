package io.github.opensabre.persistence.entity.form;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.opensabre.common.web.entity.form.BaseForm;
import io.github.opensabre.common.web.entity.convert.EntityModelConverter;
import io.github.opensabre.persistence.entity.param.BaseParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Schema
@EqualsAndHashCode(callSuper = true)
public class BaseQueryForm<P extends BaseParam> extends BaseForm {
    /**
     * 分页查询的参数，当前页数
     */
    private long current = 1;
    /**
     * 分页查询的参数，当前页面每页显示的数量
     */
    private long size = 10;

    /**
     * QueryForm转化为Param
     *
     * @param clazz Param类
     * @return 返回Param
     */
    public P toParam(Class<P> clazz) {
        return EntityModelConverter.getInstance().convert(this, clazz);
    }

    /**
     * 从form中获取page参数，用于分页查询参数
     *
     * @return 返回分页的page
     */
    public Page<P> getPage() {
        return new Page<>(this.getCurrent(), this.getSize());
    }
}
