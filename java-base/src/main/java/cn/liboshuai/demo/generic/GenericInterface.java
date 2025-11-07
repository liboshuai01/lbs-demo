package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class GenericInterface {

    private static final Logger log = LoggerFactory.getLogger(GenericInterface.class);

    @FunctionalInterface
    interface Generator<T> {
        T next();
    }

    static class StringGenerator implements Generator<String> {

        private final String[] data = {"hello", "Flink", "Generics"};

        private int index = 0;

        @Override
        public String next() {
            // 循环返回 data 数组中的内容
            if (index >= data.length) {
                index = 0;
            }
            // 返回类型必须是 String，与接口实现时指定的类型一致
            return data[index++];
        }
    }

    static class IntegerGenerator implements Generator<Integer> {

        private int current = 0;

        @Override
        public Integer next() {
            return current++;
        }
    }

    private static final Random random = new Random();

    public static void main(String[] args) {
        Generator<String> stringGenerator = new StringGenerator();
        log.info("--- stringGenerator（字符串生成器）---");
        log.info(stringGenerator.next());
        log.info(stringGenerator.next());
        log.info(stringGenerator.next());
        log.info(stringGenerator.next());
        log.info("----------------------------------------");

        Generator<Integer> integerGenerator = new IntegerGenerator();
        log.info("--- integerGenerator（整数生成器）---");
        log.info("{}", integerGenerator.next());
        log.info("{}", integerGenerator.next());
        log.info("{}", integerGenerator.next());
        log.info("{}", integerGenerator.next());

        Generator<Double> doubleGenerator = random::nextDouble;
        log.info("--- doubleGenerator（浮点数生成器）---");
        log.info("{}", doubleGenerator.next());
        log.info("{}", doubleGenerator.next());
        log.info("{}", doubleGenerator.next());
        log.info("{}", doubleGenerator.next());
    }
}
