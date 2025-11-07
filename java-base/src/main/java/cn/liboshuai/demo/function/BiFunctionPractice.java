package cn.liboshuai.demo.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

public class BiFunctionPractice {

    public static final Logger log = LoggerFactory.getLogger(BiFunctionPractice.class);

    public static String repeater(String text, Integer count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // 1. 匿名内部类方式
        BiFunction<String, Integer, String> anonymousClass = new BiFunction<String, Integer, String>() {
            @Override
            public String apply(String s, Integer integer) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < integer; i++) {
                    sb.append(s);
                }
                return sb.toString();
            }
        };
        String result1 = anonymousClass.apply("Flink", 3);
        log.info("1. 匿名内部类方式结果: {}", result1);
        log.info("---------------");

        // 2. Lambda表达式方式
        BiFunction<String, Integer, String> lambdaExpression = (text, count) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append(text);
            }
            return sb.toString();
        };
        String result2 = lambdaExpression.apply("Flink", 3);
        log.info("2. Lambda表达式方式结果: {}", result2);
        log.info("---------------");

        // 3. 方法引用方式
        BiFunction<String, Integer, String> methodRef = BiFunctionPractice::repeater;
        String result3 = methodRef.apply("Flink", 3);
        log.info("3. 方法引用方式结果: {}", result3);
        log.info("---------------");
    }
}
