package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GenericBoundType {

    private static final Logger log = LoggerFactory.getLogger(GenericBoundType.class);

    static class Generator<T extends Number> {
        private T item;

        public void setItem(T item) {
            this.item = item;
        }

        public T getItem() {
            return this.item;
        }

        public double getAsDouble() {
            return item.doubleValue();
        }
    }

    private static <T extends Comparable<T>> T findMax(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).compareTo(max) > 0) {
                max = list.get(i);
            }
        }
        return max;
    }

    public static void main(String[] args) {
        Generator<Integer> integerGenerator = new Generator<>();
        integerGenerator.setItem(100);
        Integer integerGeneratorItem = integerGenerator.getItem();
        log.info("integerGeneratorItem: {}", integerGeneratorItem);
        double integerGeneratorAsDouble = integerGenerator.getAsDouble();
        log.info("integerGeneratorAsDouble: {}", integerGeneratorAsDouble);
        log.info("------------------------------");

        Generator<Long> longGenerator = new Generator<>();
        longGenerator.setItem(200L);
        Long longGeneratorItem = longGenerator.getItem();
        log.info("longGeneratorItem: {}", longGeneratorItem);
        double longGeneratorAsDouble = longGenerator.getAsDouble();
        log.info("longGeneratorAsDouble: {}", longGeneratorAsDouble);
        log.info("------------------------------");

        List<String> stringList = List.of("Java", "Netty", "Spring", "Flink");
        String stringMax = GenericBoundType.findMax(stringList);
        log.info("stringMax: {}", stringMax);
        log.info("------------------------------");

        List<Integer> integerList = List.of(10,8,20,17);
        Integer integerMax = GenericBoundType.findMax(integerList);
        log.info("integerMax: {}", integerMax);
        log.info("------------------------------");
    }
}
