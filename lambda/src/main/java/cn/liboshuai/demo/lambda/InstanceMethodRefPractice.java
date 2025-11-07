package cn.liboshuai.demo.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class InstanceMethodRefPractice {

    public static final Logger log = LoggerFactory.getLogger(InstanceMethodRefPractice.class);

    public static void main(String[] args) {
        log.info("### 变体 A: (s) -> s.instanceMethod() ###");
        practiceVariantA();

        log.info("\n---\n");

        log.info("### 变体 B: (s1, s2) -> s1.instanceMethod(s2) ###");
        practiceVariantB();
    }

    private static void practiceVariantB() {
        List<String> stringList = Arrays.asList("Spring", "Java", "Flink", "Akka");

        // 1. 匿名内部类
        Comparator<String> anonymousClass = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        };
        log.info("1. 匿名内部类（排序前）: {}", stringList);
        stringList.sort(anonymousClass);
        log.info("1. 匿名内部类（排序后）: {}", stringList);
        log.info("------------");

        stringList = Arrays.asList("Spring", "Java", "Flink", "Akka"); // 重置列表

        // 2. lambda表达式
        Comparator<String> lambdaExpression = (o1, o2) -> o1.compareToIgnoreCase(o2);
        log.info("2. lambda表达式（排序前）: {}", stringList);
        stringList.sort(lambdaExpression);
        log.info("2. lambda表达式（排序后）: {}", stringList);
        log.info("------------");

        stringList = Arrays.asList("Spring", "Java", "Flink", "Akka"); // 重置列表

        // 3. 实例方法引用
        Comparator<String> instanceMethodRef = String::compareToIgnoreCase;
        log.info("3. 实例方法引用（排序前）: {}", stringList);
        stringList.sort(instanceMethodRef);
        log.info("3. 实例方法引用（排序后）: {}", stringList);
        log.info("------------");

        stringList = Arrays.asList("Spring", "Java", "Flink", "Akka"); // 重置列表
    }

    private static void practiceVariantA() {
        List<String> stringList = List.of("Java", "Flink", "Spring");

        // 1. 匿名内部类
        Function<String, Integer> anonymousClass = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return s.length();
            }
        };
        List<Integer> result1 = stringList.stream().map(anonymousClass).toList();
        log.info("1. 匿名内部类结果: {}", result1);
        log.info("------------");

        // 2. lambda表达式
        Function<String, Integer> lambdaExpression = s -> s.length();
        List<Integer> result2 = stringList.stream().map(lambdaExpression).toList();
        log.info("2. lambda表达式结果: {}", result2);
        log.info("------------");

        // 3. 实例方法引用
        Function<String, Integer> instanceMethodRef = String::length;
        List<Integer> result3 = stringList.stream().map(instanceMethodRef).toList();
        log.info("3. 实例方法引用结果: {}", result3);
        log.info("------------");
    }
}
