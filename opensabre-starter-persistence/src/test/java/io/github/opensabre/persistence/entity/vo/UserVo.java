package io.github.opensabre.persistence.entity.vo;

import io.github.opensabre.common.web.entity.vo.BaseVo;
import lombok.Data;

@Data
public class UserVo extends BaseVo {
    private String name;
    private String sex;
}
