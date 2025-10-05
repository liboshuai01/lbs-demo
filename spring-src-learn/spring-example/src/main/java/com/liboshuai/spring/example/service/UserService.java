package com.liboshuai.spring.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component // 标记此类为bean
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public void test() {
        LOGGER.info("调用了UserService的test方法");
    }
}
