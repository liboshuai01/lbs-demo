package cn.liboshuai.demo.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class ConsumerPractice {

    public static final Logger log = LoggerFactory.getLogger(ConsumerPractice.class);

    public static void print(String s) {
        log.info("--- 开始消费 ---");
        log.info("消费数据: {}", s.toUpperCase()); // 比如转为大写再打印
        log.info("--- 消费结束 ---");
    }

    public static void main(String[] args) {
        // 1. 匿名内部类方式
        Consumer<String> anonymousClass = new Consumer<String>() {
            @Override
            public void accept(String s) {
                log.info("--- 开始消费 ---");
                log.info("消费数据: {}", s.toUpperCase()); // 比如转为大写再打印
                log.info("--- 消费结束 ---");
            }
        };
        log.info("1. 匿名内部类方式结果:");
        anonymousClass.accept("Hello World");
        log.info("------------------");

        // 2. lambda表达式方式
        Consumer<String> lambdaExpression = s -> {
            log.info("--- 开始消费 ---");
            log.info("消费数据: {}", s.toUpperCase()); // 比如转为大写再打印
            log.info("--- 消费结束 ---");
        };
        log.info("2. lambda表达式方式结果:");
        lambdaExpression.accept("Hello World");
        log.info("------------------");

        // 3. 方法引用方式
        Consumer<String> methodRef = ConsumerPractice::print;
        log.info("3. 方法引用方式结果:");
        methodRef.accept("Hello World");
        log.info("------------------");
    }
}
