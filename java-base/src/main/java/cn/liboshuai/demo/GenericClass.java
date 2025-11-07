package cn.liboshuai.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericClass {

    private static final Logger log = LoggerFactory.getLogger(GenericClass.class);

    static class Generator<T> {
        private T item;

        public void setItem(T item) {
            this.item = item;
        }

        public T getItem() {
            return this.item;
        }
    }

    public static void main(String[] args) {
        // 1. 创建一个String泛型的Generator
        Generator<String> stringGenerator = new Generator<>();
        stringGenerator.setItem("Flink");
        String stringGeneratorItem = stringGenerator.getItem();
        log.info("stringGeneratorItem: {}", stringGeneratorItem);
        log.info("------------------");

        // 2. 创建一个Integer泛型的Generator
        Generator<Integer> integerGenerator = new Generator<>();
        integerGenerator.setItem(123);
        Integer integerGeneratorItem = integerGenerator.getItem();
        log.info("integerGeneratorItem: {}", integerGeneratorItem);
        log.info("------------------");

        // 3. 演示错误
        Generator<Double> doubleGenerator = new Generator<>();
//        doubleGenerator.setItem("xxx"); // 编译报错，double泛型不允许接收String类型的值
    }
}
