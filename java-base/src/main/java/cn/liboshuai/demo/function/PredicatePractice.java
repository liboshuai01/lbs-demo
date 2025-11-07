package cn.liboshuai.demo.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class PredicatePractice {

    public static final Logger log = LoggerFactory.getLogger(PredicatePractice.class);

    public static boolean test(String s) {
        return s.length() > 4;
    }

    public static void main(String[] args) {

        String name = "Flink";

        // 1. 匿名内部类方式
        Predicate<String> anonymousClass = new Predicate<String>() {
            @Override
            public boolean test(String s) {
                return s.length() > 4;
            }
        };
        boolean result1 = anonymousClass.test(name);
        log.info("1. 匿名内部类方式结果: {}", result1);
        log.info("---------------");

        // 2. Lambda表达式方式
        Predicate<String> lambdaExpression = s -> s.length() > 4;
        boolean result2 = lambdaExpression.test(name);
        log.info("2. Lambda表达式方式: {}", result2);
        log.info("---------------");

        // 3. 方法引用方式
        Predicate<String> methodRef = PredicatePractice::test;
        boolean result3 = methodRef.test(name);
        log.info("3. 方法引用方式: {}", result3);
        log.info("---------------");
    }
}
