package com.liboshuai.spring.mini.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationConfigApplicationContext implements ApplicationContext{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);


    public AnnotationConfigApplicationContext(Class<?> configClazz) {

    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }
}
