package org.mini.flink.api;

import java.io.Serializable;

/**
 * 收集器接口，用于从 Source 或 Operator 中向下游发送数据。
 * @param <T> 收集的数据类型
 */
@FunctionalInterface
public interface Collector<T> extends Serializable {
    void collect(T record);
}