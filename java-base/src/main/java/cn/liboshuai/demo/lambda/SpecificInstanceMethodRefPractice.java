package cn.liboshuai.demo.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SpecificInstanceMethodRefPractice {

    public static final Logger log = LoggerFactory.getLogger(SpecificInstanceMethodRefPractice.class);

    static class StringPrinter {
        public void print(String s) {
            log.info("-> [StringPrinter实例] 正在打印: {}", s);
        }
    }

    public static void main(String[] args) {

        List<String> list = Arrays.asList("Java", "Flink", "Akka", "Spring");

        StringPrinter stringPrinter = new StringPrinter();

        // 1. 匿名内部类
        Consumer<String> anonymousClass = new Consumer<String>() {
            @Override
            public void accept(String s) {
                stringPrinter.print(s);
            }
        };
        log.info("---1. 匿名内部类结果---");
        list.forEach(anonymousClass);
        log.info("---------------------");

        // 2. lambda表达式
        Consumer<String> lambdaExpression = s -> stringPrinter.print(s);
        log.info("---2. lambda表达式结果---");
        list.forEach(lambdaExpression);
        log.info("---------------------");

        // 3. 特定对象的实例方法引用
        Consumer<String> specificInstanceMethodRef = stringPrinter::print;
        log.info("---3. 特定对象的实例方法引用结果---");
        list.forEach(specificInstanceMethodRef);
        log.info("---------------------");
    }
}
