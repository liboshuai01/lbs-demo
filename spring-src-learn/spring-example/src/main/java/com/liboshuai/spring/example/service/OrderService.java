package com.liboshuai.spring.example.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component("OrderService") // 标记此类为bean
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    @Autowired // 进行依赖注入
    private UserService userService;

    public void test() {
        LOGGER.info("调用了OrderService的test方法");
        userService.test();
    }

    @PostConstruct
    public void postConstructMethod() {
        LOGGER.info("调用了OrderService的postConstructMethod方法");
    }
}