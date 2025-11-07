package cn.liboshuai.demo.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConstructorRefPractice {

    public static final Logger log = LoggerFactory.getLogger(ConstructorRefPractice.class);

    record Person(String name) {
            Person(String name) {
                this.name = name;
                log.info("-> [Person 构造器] 正在创建: {}", name);
            }

            @Override
            public String toString() {
                return "Person{" +
                        "name='" + name + '\'' +
                        '}';
            }
        }

    public static void main(String[] args) {
        System.out.println("### 变体 A: 匹配无参构造 () -> new ClassName() ###");
        practiceNoArgConstructor();

        System.out.println("\n---\n");

        System.out.println("### 变体 B: 匹配有参构造 (arg) -> new ClassName(arg) ###");
        practiceWithArgConstructor();
    }

    private static void practiceNoArgConstructor() {
        // 1. 匿名内部类
        Supplier<List<String>> anonymousClass = new Supplier<List<String>>() {
            @Override
            public List<String> get() {
                return new ArrayList<>();
            }
        };
        List<String> list1 = anonymousClass.get();
        log.info("1. 匿名内部类结果: {}", list1.getClass().getName());
        log.info("------------");

        // 2. Lambda表达式
        Supplier<List<String>> lambdaExpression = () -> new ArrayList<>();
        List<String> list2 = lambdaExpression.get();
        log.info("2. Lambda表达式结果: {}", list2.getClass().getName());
        log.info("------------");

        // 3. 构造函数引用
        Supplier<List<String>> constructorRef = ArrayList::new;
        List<String> list3 = constructorRef.get();
        log.info("3. 构造函数引用结果: {}", list3.getClass().getName());
        log.info("------------");
    }

    private static void practiceWithArgConstructor() {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

        // 1. 匿名内部类
        Function<String, Person> anonymousClass = new Function<String, Person>() {
            @Override
            public Person apply(String s) {
                return new Person(s);
            }
        };
        List<Person> result1 = names.stream().map(anonymousClass).toList();
        log.info("1. 匿名内部类结果: {}", result1);
        log.info("---------------");

        // 2. Lambda表达式
        Function<String, Person> lambdaExpression = s -> new Person(s);
        List<Person> result2 = names.stream().map(lambdaExpression).toList();
        log.info("2. Lambda表达式结果: {}", result2);
        log.info("---------------");

        // 3. 构造函数引用
        Function<String, Person> constructorRef = Person::new;
        List<Person> result3= names.stream().map(constructorRef).toList();
        log.info("3. 构造函数引用结果: {}", result3);
        log.info("---------------");
    }
}
