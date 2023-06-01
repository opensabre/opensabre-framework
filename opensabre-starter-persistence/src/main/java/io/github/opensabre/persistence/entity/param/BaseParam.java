package io.github.opensabre.persistence.entity.param;

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
    private Date createdTimeStart;
    private Date createdTimeEnd;

//    public QueryWrapper<T> build() {
//        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
//        queryWrapper.ge(null != this.createdTimeStart, "created_time", this.createdTimeStart)
//                .le(null != this.createdTimeEnd, "created_time", this.createdTimeEnd);
//        return queryWrapper;
//    }
}
