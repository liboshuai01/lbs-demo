package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UpperBoundedWildcard {
    private static final Logger log = LoggerFactory.getLogger(UpperBoundedWildcard.class);

    private static double sumOfList(List<? extends Number> list) {
        double sum = 0.0;

        for (Number number : list) {
            sum += number.doubleValue();
        }

        return sum;
    }

    public static void main(String[] args) {
        List<Integer> integerList = List.of(1, 2, 3);
        List<Long> longList = List.of(100L,200L,300L);
        List<Double> doubleList = List.of(10.10, 20.20, 30.30);
        double integerListSum = UpperBoundedWildcard.sumOfList(integerList);
        double longListSum = UpperBoundedWildcard.sumOfList(longList);
        double doubleListSum = UpperBoundedWildcard.sumOfList(doubleList);
        log.info("integerListSum: {}", integerListSum);
        log.info("longListSum: {}", longListSum);
        log.info("doubleListSum: {}", doubleListSum);
    }
}
