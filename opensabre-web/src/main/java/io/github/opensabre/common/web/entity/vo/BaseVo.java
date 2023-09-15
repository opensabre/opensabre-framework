package io.github.opensabre.common.web.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VO对象的基类
 */
@Data
@NoArgsConstructor
public class BaseVo implements Serializable {
    /**
     * VO对象唯一id
     */
    private String id;
}