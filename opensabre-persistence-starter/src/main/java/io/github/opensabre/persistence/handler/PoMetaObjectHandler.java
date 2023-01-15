package io.github.opensabre.persistence.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import io.github.opensabre.common.core.util.UserContextHolder;
import io.github.opensabre.persistence.entity.po.BasePo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Instant;
import java.util.Date;

@Slf4j
public class PoMetaObjectHandler implements MetaObjectHandler {
    /**
     * 获取当前交易的用户，为空返回默认system
     *
     * @return CurrentUsername
     */
    private String getCurrentUsername() {
        return StringUtils.defaultIfBlank(UserContextHolder.getInstance().getUsername(), BasePo.DEFAULT_USERNAME);
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdBy", String.class, getCurrentUsername());
        this.strictInsertFill(metaObject, "createdTime", Date.class, Date.from(Instant.now()));
        this.updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedBy", String.class, getCurrentUsername());
        this.strictUpdateFill(metaObject, "updatedTime", Date.class, Date.from(Instant.now()));
    }
}