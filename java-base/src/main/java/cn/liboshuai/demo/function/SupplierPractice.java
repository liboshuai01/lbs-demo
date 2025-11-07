package cn.liboshuai.demo.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class SupplierPractice {

    public static final Logger log = LoggerFactory.getLogger(SupplierPractice.class);

    public static void main(String[] args) throws InterruptedException {
        // 1. 匿名内部类方式
        Supplier<LocalDateTime> anonymousClass = new Supplier<LocalDateTime>() {
            @Override
            public LocalDateTime get() {
                return LocalDateTime.now();
            }
        };
        LocalDateTime localDateTimeFirst1 = anonymousClass.get();
        TimeUnit.MILLISECONDS.sleep(100);
        LocalDateTime localDateTimeSecond1 = anonymousClass.get();
        log.info("1. 匿名内部类方式结果一：{}", localDateTimeFirst1);
        log.info("1. 匿名内部类方式结果二：{}", localDateTimeSecond1);
        log.info("---------------");

        // 2. Lambda表达式方式
        Supplier<LocalDateTime> lambdaExpression = () -> LocalDateTime.now();
        LocalDateTime localDateTimeFirst2 = lambdaExpression.get();
        TimeUnit.MILLISECONDS.sleep(100);
        LocalDateTime localDateTimeSecond2 = lambdaExpression.get();
        log.info("2. Lambda表达式方式结果一：{}", localDateTimeFirst2);
        log.info("2. Lambda表达式方式结果二：{}", localDateTimeSecond2);
        log.info("---------------");

        // 3. 方法引用方式
        Supplier<LocalDateTime> methodRef = LocalDateTime::now;
        LocalDateTime localDateTimeFirst3 = methodRef.get();
        TimeUnit.MILLISECONDS.sleep(100);
        LocalDateTime localDateTimeSecond3 = methodRef.get();
        log.info("3. 方法引用方式结果一：{}", localDateTimeFirst3);
        log.info("3. 方法引用方式结果二：{}", localDateTimeSecond3);
        log.info("---------------");
    }
}
