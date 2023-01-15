package io.github.opensabre.common.web.entity.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class BaseForm {
    /**
     * 用户名
     */
    private String username;
}
