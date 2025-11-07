package cn.liboshuai.demo.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * 静态方法引用练习(ClassName::staticMethod)
 */
public class StaticMethodRefPractice {

    public static final Logger log = LoggerFactory.getLogger(StaticMethodRefPractice.class);

    public static Integer convertStringToInteger(String s) {
        log.info("正在执行自定义静态转换方法...");
        return Integer.parseInt(s);
    }

    public static void main(String[] args) {
        List<String> stringList = List.of("1", "2", "3");

        // 方式一：使用完整的“匿名内部类”（Java 7 的写法）
        Function<String, Integer> anonymousClass = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s);
            }
        };
        List<Integer> resultList1 = stringList.stream().map(anonymousClass).toList();
        log.info("1. 匿名内部类结果：{}", resultList1);
        log.info("---------");

        // 方式二：使用“lambda表达式”（Java 8 的写法）
        Function<String, Integer> lambdaExpression = s -> Integer.parseInt(s);
        List<Integer> resultList2 = stringList.stream().map(lambdaExpression).toList();
        log.info("2. lambda表达式结果：{}", resultList2);
        log.info("---------");

        // 方式三：使用“静态方法引用”（Java 8 的语法糖）
        Function<String, Integer> staticMethodRef = Integer::parseInt;
        List<Integer> resultList3 = stringList.stream().map(staticMethodRef).toList();
        log.info("3. 静态方法引用结果：{}", resultList3);
        log.info("---------");

        // 方式四：使用我们 *自定义* 的静态方法引用
        Function<String, Integer> customStaticMethodRef = StaticMethodRefPractice::convertStringToInteger;
        List<Integer> resultList4 = stringList.stream().map(customStaticMethodRef).toList();
        log.info("4. 自定义静态方法引用结果：{}", resultList4);
        log.info("---------");
    }
}
