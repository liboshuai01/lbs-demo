package org.mini.flink.api;

import java.io.Serializable;

/**
 * 数据汇点接口，数据流的终点。
 * @param <T> 输入数据类型
 */
@FunctionalInterface
public interface Sink<T> extends Serializable {
    void invoke(T value) throws Exception;
}