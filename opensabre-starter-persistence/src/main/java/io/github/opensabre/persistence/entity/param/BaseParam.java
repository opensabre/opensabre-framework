package io.github.opensabre.persistence.entity.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhoutaoo on 2018/6/1.
 *
 * @author zhoutaoo
 */
@Data
@NoArgsConstructor
public class BaseParam implements Serializable {
    @Schema(title = "开始时间", description = "查询条件创建记录的开始时间", requiredMode = Schema.RequiredMode.REQUIRED, example="2020-05-06 12:23:23")
    private Date createdTimeStart;
    @Schema(title = "结束时间", description = "查询条件创建记录的结束时间", requiredMode = Schema.RequiredMode.REQUIRED, example="2020-05-12 12:23:23")
    private Date createdTimeEnd;

//    public QueryWrapper<T> build() {
//        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
//        queryWrapper.ge(null != this.createdTimeStart, "created_time", this.createdTimeStart)
//                .le(null != this.createdTimeEnd, "created_time", this.createdTimeEnd);
//        return queryWrapper;
//    }
}
