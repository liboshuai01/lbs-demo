package com.liboshuai.spring.mini.example.service;

import com.liboshuai.spring.mini.context.Autowired;
import com.liboshuai.spring.mini.context.Component;
import com.liboshuai.spring.mini.context.Scope;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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