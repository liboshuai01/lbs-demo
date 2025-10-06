package com.liboshuai.spring.mini.context;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class AopBeanPostProcessor implements BeanPostProcessor {

    public static final Logger LOGGER = LoggerFactory.getLogger(AopBeanPostProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!"OrderService".equals(beanName)) {
            return bean;
        }
        // 1. 创建 Enhancer 类
        Enhancer enhancer = new Enhancer();
        // 2. 设置父类（目标类），CGLIB是通过继承来实现的
        enhancer.setSuperclass(bean.getClass());
        // 3. 设置回调（方法拦截器）
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                // 通过反射执行Before中的增强方法
                LOGGER.info("调用了AopBeanPostProcessor的postProcessAfterInitialization方法, beanName: {}", beanName);
                return method.invoke(bean, objects);
            }});
        // 4. 创建代理对象并返回
        return enhancer.create();
    }
}
