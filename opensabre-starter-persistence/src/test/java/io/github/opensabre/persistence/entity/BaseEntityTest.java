package io.github.opensabre.persistence.entity;

import io.github.opensabre.persistence.entity.form.UserForm;
import io.github.opensabre.persistence.entity.form.UserQueryForm;
import io.github.opensabre.persistence.entity.param.UserParam;
import io.github.opensabre.persistence.entity.po.User;
import io.github.opensabre.persistence.entity.vo.UserVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

class BaseEntityTest {
    @Test
    public void testQueryFormToParam() {
        UserQueryForm userQueryForm = new UserQueryForm("ZhangSan");
        userQueryForm.setCreatedTimeEnd(new Date());
        userQueryForm.setCreatedTimeStart(new Date());
        UserParam userParam = userQueryForm.toParam(UserParam.class);
        Assertions.assertEquals("ZhangSan", userParam.getName());
    }

    @Test
    public void testFormToPo() {
        UserForm userForm = new UserForm("ZhangSan");
        userForm.setAge(12);
        userForm.setSex("F");
        userForm.setBirthday(new Date());
        User user = userForm.toPo(User.class);
        Assertions.assertEquals("ZhangSan", user.getName());
    }

    @Test
    public void testPoToVo() {
        User user = new User("ZhangSan");
        user.setAge(12);
        user.setSex("F");
        user.setBirthday(new Date());
        user.setId("12");
        user.setCreatedTime(new Date());
        UserVo userVo = user.toVo(UserVo.class);
        Assertions.assertEquals("ZhangSan", userVo.getName());
    }
}