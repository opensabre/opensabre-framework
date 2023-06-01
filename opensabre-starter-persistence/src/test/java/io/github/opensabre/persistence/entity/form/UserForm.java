package io.github.opensabre.persistence.entity.form;

import io.github.opensabre.common.web.entity.form.BaseForm;
import io.github.opensabre.persistence.entity.po.User;
import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserForm extends BaseForm<User> {
    @NonNull
    private String name;
    private Integer age;
    private String sex;
    private Date birthday;
}
