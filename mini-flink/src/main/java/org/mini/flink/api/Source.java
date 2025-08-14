package org.mini.flink.api;

import java.io.Serializable;

/**
 * 数据源接口，所有数据流的起点。
 * @param <T> 输出数据的类型
 */
@FunctionalInterface
public interface Source<T> extends Serializable {
    void run(Collector<T> collector) throws Exception;
}