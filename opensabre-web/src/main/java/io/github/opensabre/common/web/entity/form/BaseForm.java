package io.github.opensabre.common.web.entity.form;

import io.github.opensabre.common.web.entity.po.BasePo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

@Schema
@Slf4j
@Data
public class BaseForm<T extends BasePo> {
    /**
     * 用户名
     */
    private String username;

    /**
     * From转化为Po，进行后续业务处理
     *
     * @param clazz 要转的Form类
     * @return 返回Po
     */
    public T toPo(Class<T> clazz) {
        T t = BeanUtils.instantiateClass(clazz);
        BeanUtils.copyProperties(this, t);
        return t;
    }

    /**
     * From转化为Po，进行后续业务处理
     *
     * @param id Form表单的id
     * @param clazz 要转的Form类
     * @return 返回Po
     */
    public T toPo(String id, Class<T> clazz) {
        T t = BeanUtils.instantiateClass(clazz);
        t.setId(id);
        BeanUtils.copyProperties(this, t);
        return t;
    }
}
