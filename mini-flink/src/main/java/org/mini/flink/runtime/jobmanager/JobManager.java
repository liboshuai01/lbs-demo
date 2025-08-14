package org.mini.flink.runtime.jobmanager;

import org.mini.flink.runtime.jobgraph.JobGraph;
import org.mini.flink.runtime.jobgraph.JobVertex;
import org.mini.flink.runtime.state.CheckpointBarrier;
import org.mini.flink.runtime.stream.DataChannel;
import org.mini.flink.runtime.stream.StreamTask;
import org.mini.flink.runtime.taskmanager.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * JobManager 是 mini-flink 的大脑，负责接收作业、生成执行计划、调度任务和协调检查点。
 */
public class JobManager {
    private static final Logger LOG = Logger.getLogger(JobManager.class.getName());
    private final TaskManager taskManager;
    private final CheckpointCoordinator checkpointCoordinator;
    private JobGraph currentJob;
    private final Map<String, List<DataChannel>> vertexChannels = new ConcurrentHashMap<>();
    private final AtomicInteger totalTasksCounter = new AtomicInteger(0);

    public JobManager(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.checkpointCoordinator = new CheckpointCoordinator(this);
    }

    /**
     * 提交作业
     * @param jobGraph 作业图
     * @return 一个代表作业最终结果的 Future
     */
    public CompletableFuture<Void> submitJob(JobGraph jobGraph) {
        LOG.info("JobManager 收到作业提交: " + jobGraph.getJobName());
        this.currentJob = jobGraph;
        totalTasksCounter.set(0);

        // 【JUC知识点】: CompletableFuture 用于表示一个异步计算的结果。
        // 这里，我们返回一个 Future，它将在作业部署完成后完成。
        // 在真实 Flink 中，它用于跟踪整个作业的生命周期（完成、失败）。
        return CompletableFuture.runAsync(() -> {
            try {
                deployJob();
                checkpointCoordinator.start(5000); // 启动检查点，每5秒一次
            } catch (Exception e) {
                throw new RuntimeException("部署作业失败!", e);
            }
        });
    }

    private void deployJob() throws Exception {
        LOG.info("开始部署作业: " + currentJob.getJobName());

        // 1. 遍历 JobVertex，创建数据通道
        for (JobVertex vertex : currentJob.getVertices()) {
            vertexChannels.put(vertex.getId(), new ArrayList<>());
        }

        // 2. 部署任务
        for (JobVertex vertex : currentJob.getVertices()) {
            int parallelism = vertex.getParallelism();
            List<String> downstreamVertexIds = currentJob.getDownstreamVertexIds(vertex.getId());

            List<DataChannel> outputChannels = new ArrayList<>();
            if (!downstreamVertexIds.isEmpty()) {
                // 如果有下游，为每个下游创建一个 channel
                for (String downstreamId : downstreamVertexIds) {
                    // TODO: 在多分发场景下，这里需要更复杂的逻辑，目前简化为一个channel
                }
                DataChannel channel = new DataChannel(1024); // 容量为1024
                outputChannels.add(channel);
                for (String downstreamId : downstreamVertexIds) {
                    vertexChannels.get(downstreamId).add(channel);
                }
            }


            for (int i = 0; i < parallelism; i++) {
                String taskName = vertex.getName() + " (" + (i + 1) + "/" + parallelism + ")";
                List<DataChannel> inputChannels = vertexChannels.get(vertex.getId());

                StreamTask task = new StreamTask(taskName, vertex.getLogic(), inputChannels, outputChannels, this);
                taskManager.submitTask(task);
                totalTasksCounter.incrementAndGet();
            }
        }
        LOG.info("作业部署完成，总共启动 " + totalTasksCounter.get() + " 个任务实例。");
    }

    public int getTotalTasks() {
        return totalTasksCounter.get();
    }

    public void triggerCheckpoint(long checkpointId) {
        LOG.info("JobManager 正在向源任务注入检查点屏障: " + checkpointId);
        CheckpointBarrier barrier = new CheckpointBarrier(checkpointId);

        // 找到所有 source 任务并注入 barrier
        for (JobVertex vertex : currentJob.getVertices()) {
            if (vertex.getLogic() instanceof org.mini.flink.api.Source) {
                // 在 mini-flink 中，Source 的输出 channel 就是下游的输入 channel
                List<DataChannel> sourceOutputChannels = findSourceOutputChannels(vertex.getId());
                for (DataChannel channel : sourceOutputChannels) {
                    // 模拟注入，真实 Flink 中有专门的 RPC 调用
                    channel.broadcastBarrier(barrier);
                }
            }
        }
    }

    private List<DataChannel> findSourceOutputChannels(String sourceVertexId) {
        List<String> downstreamIds = currentJob.getDownstreamVertexIds(sourceVertexId);
        List<DataChannel> channels = new ArrayList<>();
        if (!downstreamIds.isEmpty()) {
            // 在我们的简化模型中，一个上游对应一个输出channel，这个channel被所有下游共享
            // 所以我们只需要找到一个下游，获取它的输入channel即可
            String firstDownstreamId = downstreamIds.get(0);
            channels.addAll(vertexChannels.get(firstDownstreamId));
        }
        return channels;
    }

    public void acknowledgeCheckpoint(long checkpointId, String taskName, Map<String, Object> stateSnapshot) {
        checkpointCoordinator.acknowledgeCheckpoint(checkpointId, taskName, stateSnapshot);
    }

    public void shutdown() {
        LOG.info("JobManager 正在关闭...");
        checkpointCoordinator.stop();
        taskManager.shutdown();
    }
}