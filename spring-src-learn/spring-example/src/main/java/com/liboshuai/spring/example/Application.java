package com.liboshuai.spring.example;

import com.liboshuai.spring.example.config.AppConfig;
import com.liboshuai.spring.example.service.OrderService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {
    public static void main(String[] args) {
        // 基于AppConfig配置类，初始化Spring注解驱动的applicationContext
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        OrderService orderService = (OrderService) context.getBean("orderService");
        orderService.test();
    }
}
