package io.github.opensabre.boot.event;

import io.github.opensabre.boot.sensitive.log.desensitizer.LogBackDesensitizer;
import io.github.opensabre.boot.sensitive.log.strategy.DefaultDesensitizerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.annotation.Resource;

@Slf4j
public class OpensabreSensitiveDesensitizerProcessor implements BeanPostProcessor {

    @Resource
    private DefaultDesensitizerStrategy defaultDesensitizerStrategy;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // LogBackDesensitizer的实例自动加载
        if (bean instanceof LogBackDesensitizer) {
            log.info("postProcessAfterInitialization==> Bean Name: {}", beanName);
            defaultDesensitizerStrategy.addDesensitizer((LogBackDesensitizer) bean);
        }
        return bean;
    }
}