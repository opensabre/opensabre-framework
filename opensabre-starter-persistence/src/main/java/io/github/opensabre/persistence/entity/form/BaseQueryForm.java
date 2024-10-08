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
    @Schema(title = "当前页数", description = "分页查询的参数，当前页数", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "1")
    private long current = 1;
    /**
     * 分页查询的参数，当前页面每页显示的数量
     */
    @Schema(title = "每页数量", description = "分页查询的参数，当前页面每页显示的数量", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "10")
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
