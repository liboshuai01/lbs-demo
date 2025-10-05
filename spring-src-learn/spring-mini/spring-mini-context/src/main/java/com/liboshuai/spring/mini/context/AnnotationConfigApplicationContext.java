package com.liboshuai.spring.mini.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationConfigApplicationContext implements ApplicationContext{

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationConfigApplicationContext.class);

    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    public AnnotationConfigApplicationContext(Class<?> configClazz) {
        // 1. 扫描@ComponentScan指定包路径下的所有bean，存放到beanDefinitionMap中 (BeanDefinition表示bean的定义信息）
        scanBeanDefinition(configClazz);
        
        // 2. 创建所有单例非懒加载bean，存放到singletonObjects中
        
    }

    private void scanBeanDefinition(Class<?> configClazz) {
        // 效验一个传入的配置类
        verifyConfig(configClazz);
        String componentScanValue = configClazz.getAnnotation(ComponentScan.class).value();

    }

    private static void verifyConfig(Class<?> configClazz) {
        if (!configClazz.isAnnotationPresent(Configuration.class)) {
            throw new IllegalStateException(configClazz + "不是一个配置类，请使用@Configuration注解标注");
        }
        if (!configClazz.isAnnotationPresent(ComponentScan.class)) {
            throw new IllegalStateException(configClazz + "请使用@ComponentScan注解标注");
        }
        String componentScanValue = configClazz.getAnnotation(ComponentScan.class).value();
        if (componentScanValue == null || componentScanValue.trim().isEmpty()) {
            throw new IllegalStateException(configClazz + "的@ComponentScan注解请传入对应值");
        }
    }

    @Override
    public Object getBean(String beanName) {
        /*
            1. 单例非懒加载的bean直接从singletonObjects中get即可
            2. 单例懒加载的bean需要现在创建，并放入到singletonObjects中
            3. 多例bean每get一次就创建一次，且不需要放到singletonObjects中
         */
        return null;
    }
}
