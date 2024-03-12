package io.github.opensabre.persistence.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.opensabre.common.web.entity.convert.EntityModelConverter;
import io.github.opensabre.common.web.entity.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class BasePo<V extends BaseVo> implements Serializable {
    public final static String DEFAULT_USERNAME = "system";

    @Schema(title = "主键", description = "存储数据记录的唯一ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100001")
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @Schema(title = "创建人", description = "创建该数据记录人的唯一ID，默认为system", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @Schema(title = "创建时间", description = "创建该数据记录的时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2021-04-09 21:00:00")
    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    @Schema(title = "更新人", description = "更新该数据记录人的唯一ID，默认为system", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @Schema(title = "更新时间", description = "更新该数据记录的时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2021-04-10 21:00:00")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedTime;

    /**
     * Po转化为Vo
     *
     * @param clazz Vo类
     * @return 返回Vo
     */
    public V toVo(Class<V> clazz) {
        return EntityModelConverter.getInstance().convert(this, clazz);
    }
}
