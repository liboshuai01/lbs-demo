package com.liboshuai.spring.mini.example.service;

import com.liboshuai.spring.mini.context.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Scope("prototype")
@Component("OrderService") // 标记此类为bean
public class OrderService implements InitializingBean, BeanNameAware, ApplicationContextAware {

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

    @Override
    public void afterPropertiesSet() {
        LOGGER.info("调用了InitializingBean的afterPropertiesSet方法");
    }

    @Override
    public void setBeanName(String name) {
        LOGGER.info("调用了BeanNameAware的setBeanName方法, beanName: {}", name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        int beanDefinitionCount = applicationContext.getBeanDefinitionCount();
        LOGGER.info("调用了ApplicationContextAware的setApplicationContext方法, beanDefinitionCount: {}", beanDefinitionCount);
    }
}