package com.liboshuai.spring.mini.context;

public class AopBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
