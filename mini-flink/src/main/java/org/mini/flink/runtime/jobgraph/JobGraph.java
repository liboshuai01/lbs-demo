package org.mini.flink.runtime.jobgraph;


import java.io.Serializable;
import java.util.*;

/**
 * 作业图，描述了一个完整的流处理作业的拓扑结构 (DAG)。
 */
public class JobGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String jobId = UUID.randomUUID().toString();
    private final String jobName;
    private final List<JobVertex> vertices = new ArrayList<>();
    // 使用邻接表表示DAG, key 是上游 vertex id, value 是下游 vertex id 列表
    private final Map<String, List<String>> edges = new HashMap<>();

    public JobGraph(String jobName) {
        this.jobName = jobName;
    }

    public void addVertex(JobVertex vertex) {
        vertices.add(vertex);
    }

    public void addEdge(JobVertex upstream, JobVertex downstream) {
        edges.computeIfAbsent(upstream.getId(), k -> new ArrayList<>()).add(downstream.getId());
    }

    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public List<JobVertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<String> getDownstreamVertexIds(String vertexId) {
        return edges.getOrDefault(vertexId, Collections.emptyList());
    }
}