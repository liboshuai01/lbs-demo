package org.mini.flink.runtime.state;

import java.io.Serializable;

/**
 * 检查点屏障，一个特殊的数据元素，在数据流中流动以触发检查点。
 */
public class CheckpointBarrier implements Serializable {
    private static final long serialVersionUID = 1L;
    private final long checkpointId;

    public CheckpointBarrier(long checkpointId) {
        this.checkpointId = checkpointId;
    }

    public long getCheckpointId() {
        return checkpointId;
    }

    @Override
    public String toString() {
        return "CheckpointBarrier-" + checkpointId;
    }
}