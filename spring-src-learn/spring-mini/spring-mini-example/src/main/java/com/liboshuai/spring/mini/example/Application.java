package com.liboshuai.spring.mini.example;

import com.liboshuai.spring.mini.context.AnnotationConfigApplicationContext;
import com.liboshuai.spring.mini.example.config.AppConfig;
import com.liboshuai.spring.mini.example.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    public static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // 基于AppConfig配置类，初始化Spring注解驱动的applicationContext
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        OrderService orderService = (OrderService) context.getBean("OrderService");
        orderService.test();
        LOGGER.info("bean: {}", context.getBean("OrderService"));
        LOGGER.info("bean: {}", context.getBean("OrderService"));
    }
}
