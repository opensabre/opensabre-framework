package io.github.opensabre.persistence.entity.param;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class UserParam extends BaseParam {
    private String name;
}



