package com.liboshuai.spring.mini.context;

public interface ApplicationContext {
    /**
     * 根据bean名称获取bean示例对象
     */
    Object getBean(String beanName);

    /**
     * 获取BeanDefinition的数量
     */
    int getBeanDefinitionCount();
}
