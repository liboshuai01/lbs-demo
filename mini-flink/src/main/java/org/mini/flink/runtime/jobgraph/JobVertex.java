package org.mini.flink.runtime.jobgraph;

import java.io.Serializable;
import java.util.UUID;

/**
 * 作业图中的一个顶点，代表一个任务（Source, Operator, or Sink）。
 */
public class JobVertex implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id = UUID.randomUUID().toString();
    private final String name;
    private final Serializable logic; // 持有 Source, Operator, 或 Sink 的逻辑
    private final int parallelism;

    public JobVertex(String name, Serializable logic, int parallelism) {
        this.name = name;
        this.logic = logic;
        this.parallelism = parallelism;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Serializable getLogic() {
        return logic;
    }

    public int getParallelism() {
        return parallelism;
    }
}