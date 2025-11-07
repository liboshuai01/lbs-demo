package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GenericMethod {

    private static final Logger log = LoggerFactory.getLogger(GenericMethod.class);

    static class Utils {
        public static <T> void printArray(T[] array) {
            log.info("--- 开始打印 {} ---", array.getClass().getSimpleName());
            if (array.length == 0) {
                log.info("(数组为空)");
                return;
            }
            for (T element : array) {
                log.info("element: {}", element);
            }
            log.info("--- 打印完毕 ---");
        }

        public static <K,V> void printKeyValuePair(K key, V value) {
            log.info("键(Key): {}, 值(Value): {}", key, value);
        }

        public static <T> T getFist(List<T> list) {
            if (list == null || list.isEmpty()) {
                return null;
            }
            return list.get(0);
        }
    }

    public static void main(String[] args) {
        String[] strings = new String[]{"flink", "java", "spring"};
        Integer[] integers = new Integer[]{1,2,3};

        Utils.printArray(strings);
        Utils.printArray(integers);

        Utils.printKeyValuePair("one", 1);
        Utils.printKeyValuePair(2, "two");

        String fist = Utils.getFist(List.of("Flink", "Spring"));
        log.info("fist: {}", fist);
    }
}





























