package cn.liboshuai.demo.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * 示例 1: Function<T, R> (转换型)
 * * 演示 Function 接口，它接收一个输入（T类型），并返回一个输出（R类型）。
 */
public class FunctionPractice {

    public static final Logger log = LoggerFactory.getLogger(FunctionPractice.class);

    public static void main(String[] args) {

        String name = "Flink";

        // 1. 匿名内部类方式定义并使用Function
        Function<String, Integer> anonymousClass = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return s.length();
            }
        };
        Integer result1 = anonymousClass.apply(name);
        log.info("1. 匿名内部类方式结果: {}", result1);
        log.info("---------------");

        // 2. lambda表达式方式定义并使用Function
        Function<String, Integer> lambdaExpression = s -> s.length();
        Integer result2 = lambdaExpression.apply(name);
        log.info("2. lambda表达式方式结果: {}", result2);
        log.info("---------------");

        // 3. 方法引用方式定义并使用Function
        Function<String, Integer> methodRef = String::length;
        Integer result3 = methodRef.apply(name);
        log.info("3. 方法引用方式结果: {}", result3);
        log.info("---------------");
    }
}
