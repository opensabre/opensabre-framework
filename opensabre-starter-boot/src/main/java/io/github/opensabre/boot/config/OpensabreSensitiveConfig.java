package io.github.opensabre.boot.config;

import io.github.opensabre.boot.event.OpensabreSensitiveDesensitizerProcessor;
import io.github.opensabre.boot.sensitive.log.LogBackCoreConverter;
import io.github.opensabre.boot.sensitive.log.desensitizer.LogBackDesensitizer;
import io.github.opensabre.boot.sensitive.log.desensitizer.RegxLogBackDesensitizer;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 初使化脱敏配置
 */
@AutoConfiguration
@ConditionalOnProperty(value = "opensabre.sensitive.log.enabled", havingValue = "true")
public class OpensabreSensitiveConfig {

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return configurableListableBeanFactory -> {
            LogBackCoreConverter logBackCoreConverter = LogBackCoreConverter.getInstance();
            configurableListableBeanFactory.registerSingleton("logBackCoreConverter", logBackCoreConverter);
            configurableListableBeanFactory.registerSingleton("defaultDesensitizerStrategy", logBackCoreConverter.getDesensitizerStrategy());
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public LogBackDesensitizer regxLogBackDesensitizer() {
        return new RegxLogBackDesensitizer();
    }

    @Bean
    public BeanPostProcessor opensabreSensitiveDesensitizerProcessor() {
        return new OpensabreSensitiveDesensitizerProcessor();
    }
}