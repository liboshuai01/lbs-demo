package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UnboundedWildcard {
    private static final Logger log = LoggerFactory.getLogger(UnboundedWildcard.class);

    private static void printListSize(List<?> list) {
        log.info("List 的大小是: {}", list.size());
    }

    public static void main(String[] args) {
        List<String> stringList = List.of("Java", "Flink", "Tomcat", "Netty");
        List<Integer> integerList = List.of(1,2,3,4,5);
        UnboundedWildcard.printListSize(stringList);
        UnboundedWildcard.printListSize(integerList);
    }
}
