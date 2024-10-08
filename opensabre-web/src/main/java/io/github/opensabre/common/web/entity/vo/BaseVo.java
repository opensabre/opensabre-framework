package io.github.opensabre.common.web.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(title = "对象唯一id", description = "对象唯一id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;
}