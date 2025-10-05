package com.liboshuai.spring.mini.context;

/**
 * Bean定义信息
 */
public class BeanDefinition {
    private final Class<?> beanClass;
    private final String scope;
    private final boolean lazy;


    public BeanDefinition(Class<?> beanClass, String scope, boolean lazy) {
        this.beanClass = beanClass;
        this.scope = scope;
        this.lazy = lazy;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getScope() {
        return scope;
    }

    public boolean isLazy() {
        return lazy;
    }
}
