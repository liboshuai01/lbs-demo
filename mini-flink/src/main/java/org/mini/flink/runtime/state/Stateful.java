package org.mini.flink.runtime.state;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 标识一个算子是可拥有状态的。
 * 使用 ConcurrentHashMap 作为最简单的键/值状态存储。
 * Flink 中有更复杂的状态后端 (StateBackend)。
 */
public interface Stateful extends Serializable {

    /**
     * 初始化状态
     */
    void initializeState(Map<String, Object> state);

    /**
     * 对当前状态进行快照
     * @return 状态的快照
     */
    Map<String, Object> snapshotState();
}