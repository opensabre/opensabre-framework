package io.github.opensabre.persistence.entity.form;

import io.github.opensabre.persistence.entity.param.UserParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserQueryForm extends BaseQueryForm<UserParam> {
    @NonNull
    private String name;
    private Date createdTimeStart;
    private Date createdTimeEnd;
}
