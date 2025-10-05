package com.liboshuai.spring.mini.example;

import com.liboshuai.spring.mini.context.AnnotationConfigApplicationContext;
import com.liboshuai.spring.mini.context.ApplicationContext;
import com.liboshuai.spring.mini.example.config.AppConfig;
import com.liboshuai.spring.mini.example.service.OrderService;

public class Application {
    public static void main(String[] args) {
        // 基于AppConfig配置类，初始化Spring注解驱动的applicationContext
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        OrderService orderService = (OrderService) context.getBean("orderService");
        orderService.test();
    }
}
