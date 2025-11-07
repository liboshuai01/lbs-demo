package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LowerBoundedWildcard {
    private static final Logger log = LoggerFactory.getLogger(LowerBoundedWildcard.class);

    private static void addIntegers(List<? super Integer> list) {
        list.add(10);
        list.add(20);
        list.add(30);
        log.info(" (成功向 {} 添加了 3 个整数)", list.getClass().getSimpleName());
        Object o = list.get(0);
        log.info(" (只能安全地读出 Object: {})", o);
    }

    public static void main(String[] args) {
        List<Integer> integerList = new ArrayList<>();
        List<Number> numberList = new ArrayList<>();
        List<Object> objectList = new ArrayList<>();

        log.info("--- 操作 List<Integer> ---");
        LowerBoundedWildcard.addIntegers(integerList);
        log.info("结果: {}", integerList);
        log.info("--- 操作 List<Number> ---");
        LowerBoundedWildcard.addIntegers(numberList);
        log.info("结果: {}", numberList);
        log.info("--- 操作 List<Object> ---");
        LowerBoundedWildcard.addIntegers(objectList);
        log.info("结果: {}", objectList);
    }
}
