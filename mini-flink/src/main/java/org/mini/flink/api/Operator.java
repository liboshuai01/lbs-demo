package org.mini.flink.api;

import java.io.Serializable;

/**
 * 处理数据的算子接口，例如 map, filter 等。
 * @param <IN> 输入数据类型
 * @param <OUT> 输出数据类型
 */
@FunctionalInterface
public interface Operator<IN, OUT> extends Serializable {
    void process(IN element, Collector<OUT> collector) throws Exception;
}