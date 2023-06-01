package io.github.opensabre.persistence.entity.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.github.opensabre.common.web.entity.convert.EntityModelConverter;
import io.github.opensabre.common.web.entity.vo.BaseVo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
public class BasePo<V extends BaseVo> implements Serializable {
    public final static String DEFAULT_USERNAME = "system";
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createdTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

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
