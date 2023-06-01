package io.github.opensabre.persistence.entity.po;

import io.github.opensabre.persistence.entity.vo.UserVo;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BasePo<UserVo> {
    @NonNull
    private String name;
    private Integer age;
    private String sex;
    private Date birthday;
}