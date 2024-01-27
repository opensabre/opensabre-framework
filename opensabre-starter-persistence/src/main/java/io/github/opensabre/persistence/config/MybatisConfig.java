package io.github.opensabre.persistence.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.IllegalSQLInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import io.github.opensabre.persistence.exception.PersistenceExceptionHandlerAdvice;
import io.github.opensabre.persistence.handler.PoMetaObjectHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@AutoConfiguration
@EnableTransactionManagement
@Import(PersistenceExceptionHandlerAdvice.class)
@PropertySource(value = "classpath:opensabre-persistence.properties", encoding = "UTF8")
public class MybatisConfig {

    @Value("${opensabre.persistence.interceptor.blockattack.enabled}")
    private Boolean blockattackEnabled;
    @Value("${opensabre.persistence.interceptor.illegalsql.enabled}")
    private Boolean illegalsqlEnabled;
    @Value("${opensabre.persistence.interceptor.pagination.enabled}")
    private Boolean paginationEnabled;
    @Value("${opensabre.persistence.interceptor.pagination.dbType}")
    private String paginationDbType;

    /**
     * 初使化Mybatis审计字段自动赋值
     */
    @Bean
    public PoMetaObjectHandler poMetaObjectHandler() {
        return new PoMetaObjectHandler();
    }

    /**
     * mybatis插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        if (paginationEnabled) {
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.getDbType(paginationDbType)));
        }
        // 防止全表更新与删除
        if (blockattackEnabled) {
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        }
        // sql 性能规范插件
        if (illegalsqlEnabled) {
            interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());
        }
        return interceptor;
    }
}
