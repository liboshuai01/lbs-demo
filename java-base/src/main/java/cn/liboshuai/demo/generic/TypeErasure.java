package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TypeErasure {
    private static final Logger log = LoggerFactory.getLogger(TypeErasure.class);

    public static void main(String[] args) {
        List<String> stringList = new ArrayList<>();
        List<Integer> integerList = new ArrayList<>();

        Class<? extends List> stringListClass = stringList.getClass();
        Class<? extends List> integerListClass = integerList.getClass();
        if (stringListClass.equals(integerListClass)) {
            log.info("结论：在运行时，List<String> 和 List<Integer> 是同一个 Class 对象 (java.util.ArrayList)。");
            log.info("泛型信息 <String> 和 <Integer> 被擦除了。");
        }
    }
}
